/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.shared.ErrorList;
import com.google.common.net.InetAddresses;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.UUID;

public class IpResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(IpResource.class);

    private static final String SQL_INSERT_STATUS =
            "INSERT INTO status(client_uuid,time,plattform,model,product,device,software_version_code,api_level,ip,"
                    + "age,lat,long,accuracy,altitude,speed,provider)"
                    + "VALUES(?, NOW(),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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

        logger.info("IP request from " + clientIpRaw);

        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try {
                // debug parameters sent
                request = new JSONObject(entity);
                logger.debug(request.toString(4));
//            	System.out.println(request.toString(4));

            	/* sample request data
                {
            	    "api_level": "21",
            	    "device": "hammerhead",
            	    "language": "en",
            	    "model": "Nexus 5",
            	    "os_version": "5.0(1570415)",
            	    "plattform": "Android",
            	    "product": "hammerhead",
            	    "softwareRevision": "master_initial-2413-gf89049d",
            	    "softwareVersionCode": 20046,
            	    "softwareVersionName": "2.0.46",
            	    "timezone": "Europe/Vienna",
            	    "type": "MOBILE",
            	    "uuid": "........(uuid)........"
            	    "location": {
        				"accuracy": 20,
        				"age": 7740,
        				"lat": 51.1053539,
        				"long": 17.4921002,
        				"provider": "network"
    				},
            	}
            	*/
                UUID uuid = null;
                final String uuidString = request.optString("uuid", "");
                try {
                    if (uuidString.length() != 0)
                        uuid = UUID.fromString(uuidString);
                } catch (IllegalArgumentException iae){
                    errorList.addErrorString("Invalid UUID");
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    answer.put("error",errorList.getList());
                    logger.error(answer.toString());
                    return answer.toString();
                }

                final String clientPlattform = request.getString("plattform");
                final String clientModel = request.getString("model");
                final String clientProduct = request.getString("product");
                final String clientDevice = request.getString("device");
                final String clientSoftwareVersionCode = request.getString("softwareVersionCode");
                final String clientApiLevel = request.getString("api_level");

                final JSONObject location = request.optJSONObject("location");

                long geoage = 0; // age in ms
                double geolat = 0;
                double geolong = 0;
                float geoaccuracy = 0; // in m
                double geoaltitude = 0;
                float geospeed = 0; // in m/s
                String geoprovider = "";

                if (!request.isNull("location")) {
                    geoage = location.optLong("age", 0);
                    geolat = location.optDouble("lat", 0);
                    geolong = location.optDouble("long", 0);
                    geoaccuracy = (float) location.optDouble("accuracy", 0);
                    geoaltitude = location.optDouble("altitude", 0);
                    geospeed = (float) location.optDouble("speed", 0);
                    geoprovider = location.optString("provider", "");
                }


                if (errorList.getLength() == 0)
                    try {
                        PreparedStatement st;
                        st = conn
                                .prepareStatement(
                                        SQL_INSERT_STATUS,
                                        Statement.RETURN_GENERATED_KEYS);
                        int i = 1;
                        st.setObject(i++, uuid);
                        st.setObject(i++, clientPlattform);
                        st.setObject(i++, clientModel);
                        st.setObject(i++, clientProduct);
                        st.setObject(i++, clientDevice);
                        st.setObject(i++, clientSoftwareVersionCode);
                        st.setObject(i++, clientApiLevel);
                        st.setObject(i++, clientIpRaw);
                        // location information
                        st.setObject(i++, geoage);
                        st.setObject(i++, geolat);
                        st.setObject(i++, geolong);
                        st.setObject(i++, geoaccuracy);
                        st.setObject(i++, geoaltitude);
                        st.setObject(i++, geospeed);
                        st.setObject(i++, geoprovider);

                        logger.debug(st.toString());
                        final int affectedRows = st.executeUpdate();
                        if (affectedRows == 0)
                            errorList.addError("ERROR_DB_STORE_STATUS");
                    } catch (final SQLException e) {
                        errorList.addError("ERROR_DB_STORE_GENERAL");
                        logger.error(e.getMessage());
                    }

                answer.put("ip", clientIpRaw);
                if (clientAddress instanceof Inet4Address) {
                    answer.put("v", "4");
                } else if (clientAddress instanceof Inet6Address) {
                    answer.put("v", "6");
                } else {
                    answer.put("v", "0");
                }
            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSON Data " + e.toString());
            }
        } else {
            errorList.addErrorString("Expected request is missing.");
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