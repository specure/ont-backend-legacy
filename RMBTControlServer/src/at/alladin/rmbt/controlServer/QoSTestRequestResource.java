/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.db.QoSTestObjective;
import at.alladin.rmbt.shared.db.dao.QoSTestObjectiveDao;
import at.alladin.rmbt.shared.json.LocationJson;
import at.alladin.rmbt.shared.json.TestServerJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
import at.alladin.rmbt.shared.qos.testscript.TestScriptInterpreter;
import com.google.common.net.InetAddresses;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author lb
 */
public class QoSTestRequestResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(QoSTestRequestResource.class);

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);
        final String clientIpString = InetAddresses.toAddrString(clientAddress);

        logger.info("New QoS-test request from " + clientIpRaw);

        final String geoIpCountry = GeoIPHelper.getInstance().lookupCountry(clientAddress);
        // public_ip_asn
        final Long asn = Helperfunctions.getASN(clientAddress);
        // public_ip_as_name
        // country_asn (2 digit country code of AS, eg. AT or EU)
        final String asCountry;
        if (asn == null) {
            asCountry = null;
        } else {
            asCountry = Helperfunctions.getAScountry(asn);
        }

        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                TestServerJsonInterface server = null;
                server = getQoSServerByUserID(UUID.fromString(request.getString("uuid")), true, false);

                if (server == null) {
                    LocationJson locationJson = null;
                    if (request.has("location")) {
                        locationJson = new LocationJson(request.getJSONObject("location"));
                    } else {
                        locationJson = new LocationJson();
                        locationJson.setGeo_lat((double) GeoIPHelper.getInstance().getLatitudeFromIP(clientAddress));
                        locationJson.setGeo_long((double) GeoIPHelper.getInstance().getLongitudeFromIP(clientAddress));
                    }

                    server = getNearestServer(locationJson.getGeo_lat(),
                            locationJson.getGeo_long(), clientIpString, asCountry,
                            geoIpCountry, "QoS", true, false).get(0);
                }

                logger.debug("Nearest QoS server for country: " + geoIpCountry + ". asCountry: "
                        + asCountry + " id:" + ((TestServerJson)server).getId() + " name:" + ((TestServerJson)server).getName() + " address:"
                        + ((TestServerJson)server).getAddress() + " port:" + ((TestServerJson)server).getPort());

                List<QoSTestObjective> listTestParams = new ArrayList<>();
                QoSTestObjectiveDao testObjectiveDao = new QoSTestObjectiveDao(conn);
                listTestParams = testObjectiveDao.getByTestClass(((TestServerJson)server).getId(), 1);
                // listTestParams.add(testObjectiveDao.getById(1));
                Map<String, List<JSONObject>> tests = new HashMap<>();

                for (QoSTestObjective o : listTestParams) {
                    List<JSONObject> testList;

                    if (tests.containsKey(o.getTestType())) {
                        testList = tests.get(o.getTestType());
                    } else {
                        testList = new ArrayList<>();
                        tests.put(o.getTestType(), testList);
                    }

                    JSONObject params = new JSONObject(o.getObjective());

                    Iterator<String> keys = params.keys();
                    boolean testInvalid = false;

                    // iterate through all keys and interprete their values if necessary;
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object scriptResult = TestScriptInterpreter.interprete(params.getString(key), null);
                        if (scriptResult != null) {
                            params.put(key, String.valueOf(scriptResult));
                        } else {
                            testInvalid = true;
                            break;
                        }
                    }

                    // add test uid to the params object
                    params.put("qos_test_uid", String.valueOf(o.getUid()));
                    params.put("concurrency_group", String.valueOf(o.getConcurrencyGroup()));
                    if (clientAddress instanceof Inet6Address) {
                        params.put("server_addr", String.valueOf(o.getTestServerIpv6()));
                    } else {
                        params.put("server_addr", String.valueOf(o.getTestServerIpv4()));
                    }
                    params.put("server_port", String.valueOf(o.getPort()));

                    if (!testInvalid) {
                        testList.add(params);
                    }
                }

                answer.put("objectives", tests);

                // System.out.println(answer);

                answer.put("test_duration", getSetting("rmbt_duration"));
                answer.put("test_numthreads", getSetting("rmbt_num_threads"));
                answer.put("test_numpings", getSetting("rmbt_num_pings"));
                answer.put("client_remote_ip", clientIpString);

            } catch (JSONException | SQLException e) {
                logger.error(e.toString());
                errorList.addError("ERROR_DB_QOS_GET_OBJECTIVE_NOT_FOUND");
            }
        }

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        answerString = answer.toString();

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }


    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}
