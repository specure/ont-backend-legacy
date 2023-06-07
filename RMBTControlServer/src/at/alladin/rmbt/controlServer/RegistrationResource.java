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
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.db.Client;
import at.alladin.rmbt.shared.db.GeoLocation;
import at.alladin.rmbt.shared.json.TestServerJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
import com.google.common.net.InetAddresses;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

public class RegistrationResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(RegistrationResource.class);

    private static final String SELECT_SERVER_FOR_ID = "SELECT * FROM TEST_SERVER WHERE UID = ?";
    private static final String SELECT_SECRET_KEY_FOR_SERVER_ID = "SELECT SECRET_KEY FROM TEST_SERVER WHERE UID = ?";
    private static final String INSERT_INTO_TEST = "INSERT INTO test(time, uuid, open_test_uuid, client_id, client_name, client_version, client_software_version, client_language, client_public_ip, client_public_ip_anonymized, country_geoip, server_id, port, use_ssl, timezone, client_time, duration, num_threads_requested, status, software_revision, client_test_counter, client_previous_test_status, public_ip_asn, public_ip_as_name, country_asn, public_ip_rdns, run_ndt) " +
            "VALUES(NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_NEXT_TEST_SLOT = "SELECT rmbt_get_next_test_slot(?)";
    private static final String SELECT_PROVIDER = "SELECT rmbt_set_provider_from_as(?)";
    private static final String UPDATE_TOKEN = "UPDATE test SET token = ? WHERE uid = ?";

    // ONT-4704 - [Legacy] CS is not checking the version of the test server
    private final Boolean checkMeasurementServerVersion = CustomerResource.getInstance().checkMeasurementServerVersion();

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        long startTime = System.currentTimeMillis();
        String secretKey = null;
        //final String defaultSecretKey = getContext().getParameters().getFirstValue("RMBT_SECRETKEY");

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);
        final String clientIpString = InetAddresses.toAddrString(clientAddress);

        logger.info("New test request from " + clientIpRaw);

        final String geoIpCountry = GeoIPHelper.getInstance().lookupCountry(clientAddress);
        // public_ip_asn
        final Long asn = Helperfunctions.getASN(clientAddress);
        // public_ip_as_name
        // country_asn (2 digit country code of AS, eg. AT or EU)
        final String asName;
        final String asCountry;
        if (asn == null) {
            asName = null;
            asCountry = null;
        } else {
            asName = Helperfunctions.getASName(asn);
            asCountry = Helperfunctions.getAScountry(asn);
        }

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                int typeId = 0;

                final String lang = request.optString("language");

                // Load Language Files for Client

                final List<String> langs =
                        Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                }

                // System.out.println(request.toString(4));

                if (conn != null) {

                    final Client clientDb = new Client(conn);

                    if (!request.optString("type").isEmpty()) {
                        typeId = clientDb.getTypeId(request.getString("type"));
                        if (clientDb.hasError())
                            errorList.addError(clientDb.getError());
                    }

                    final List<String> clientNames =
                            Arrays.asList(settings.getString("RMBT_CLIENT_NAME").split(",\\s*"));
                    final List<String> clientVersions =
                            Arrays.asList(settings.getString("RMBT_VERSION_NUMBER").split(",\\s*"));

                    // ONT-4704 - [Legacy] CS is not checking the version of the test server
//                    if (clientNames.contains(request.optString("client"))
//                            && clientVersions.contains(request.optString("version")) && typeId > 0) {
                    if (!checkMeasurementServerVersion || (clientNames.contains(request.optString("client"))
                            && clientVersions.contains(request.optString("version")) && typeId > 0)) {

                        UUID uuid = null;
                        final String uuidString = request.optString("uuid", "");
                        try {
                            if (uuidString.length() != 0)
                                uuid = UUID.fromString(uuidString);
                        }catch (IllegalArgumentException iae){
                            errorList.addErrorString("Invalid UUID");
                            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            answer.put("error",errorList.getList());
                            logger.error(answer.toString());
                            return answer.toString();
                        }


                        final String clientName = request.getString("client");
                        final String clientVersion = request.getString("version");

                        String timeZoneId = request.getString("timezone");
                        // String tmpTimeZoneId = timeZoneId;

                        final long clientTime = request.getLong("time");
                        final Timestamp clientTstamp =
                                java.sql.Timestamp.valueOf(new Timestamp(clientTime).toString());

                        final JSONObject location = request.optJSONObject("location");

                        long geotime = 0;
                        Double geolat = Double.MAX_VALUE;
                        Double geolong = Double.MAX_VALUE;
                        float geoaccuracy = 0;
                        double geoaltitude = 0;
                        float geobearing = 0;
                        float geospeed = 0;
                        String geoprovider = "";

                        if (!request.isNull("location")) {
                            // locationJson = new LocationJson();
                            geotime = location.optLong("time", 0);
                            geolat = location.optDouble("lat", 0);
                            geolong = location.optDouble("long", 0);
                            geoaccuracy = (float) location.optDouble("accuracy", 0);
                            geoaltitude = location.optDouble("altitude", 0);
                            geobearing = (float) location.optDouble("bearing", 0);
                            geospeed = (float) location.optDouble("speed", 0);
                            geoprovider = location.optString("provider", "");
                        }else{
                            geolat = (double)GeoIPHelper.getInstance().getLatitudeFromIP(clientAddress);
                            geolong = (double)GeoIPHelper.getInstance().getLongitudeFromIP(clientAddress);
                        }

                        Calendar timeWithZone = null;

                        if (timeZoneId.isEmpty()) {
                            timeZoneId = Helperfunctions.getTimezoneId();
                            timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
                        } else
                            timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);

                        long clientUid = 0;
            /*
             * if (uuid == null) { clientDb.setTimeZone(timeWithZone); clientDb.setTime(tstamp);
             * clientDb.setClient_type_id(typeId); uuid = clientDb.storeClient(); if
             * (clientDb.hasError()) { errorList.addError(clientDb.getError()); } else {
             * answer.put("uuid", uuid.toString()); } }
             */

                        if (errorList.getLength() == 0 && uuid != null) {
                            clientUid = clientDb.getClientByUuid(uuid);
                            if (clientDb.hasError())
                                errorList.addError(clientDb.getError());
                        }

                        if (clientUid > 0) {

                            final String testUuid = UUID.randomUUID().toString();
                            final String testOpenUuid = UUID.randomUUID().toString();

                            boolean testServerEncryption = true; // default is
                            // true

                            // hack for android api <= 10 (2.3.x)
                            // using encryption with test doesn't work
                            // can be deleted
                            if (request.has("plattform") && request.optString("plattform").equals("Android"))
                                if (request.has("api_level")) {
                                    final String apiLevelString = request.optString("api_level");
                                    try {
                                        final int apiLevel = Integer.parseInt(apiLevelString);
                                        if (apiLevel <= 10)
                                            testServerEncryption = false;
                                    } catch (final NumberFormatException e) {
                                    }
                                }


                            final String serverType;
                            if (request.optString("client").equals("RMBTws"))
                                serverType = "RMBTws";
                            else
                                serverType = "RMBT";

                            Boolean ipv6 = null;
                            if (clientAddress instanceof Inet6Address)
                                ipv6 = true;
                            else if (clientAddress instanceof Inet4Address)
                                ipv6 = false;
                            else // should never happen, unless ipv > 6 is available
                                ipv6 = null;

                            logger.info("IPV6 VALUE: " + ipv6);

                            // final TestServerJson server =
                            // measurementserver.getNearestServer(locationJson.getLat(), locationJson.getLng(),
                            // locationJson.getTime(),
                            // clientIpString, asCountry, geoIpCountry, serverType, testServerEncryption, ipv6);

                            // final TestServerJson server = measurementserver.getNearestServer(geolat, geolong,
                            // geotime,
                            // clientIpString, asCountry, geoIpCountry, serverType, testServerEncryption, ipv6);

                            TestServerJsonInterface server = null;

                            if (request.has("measurement_server_id")
                                    && (request.optInt("measurement_server_id") != -1)) {
                                logger.info("Server was selected");
                                try (PreparedStatement ps = conn.prepareStatement(SELECT_SERVER_FOR_ID)) {
                                    ps.setInt(1, request.getInt("measurement_server_id"));
                                    logger.debug(ps.toString());
                                    ResultSet rs = ps.executeQuery();

                                    while (rs.next()) {
                                        secretKey = rs.getString("secret_key");
                                        server = new TestServerJson(rs.getInt("uid"), rs.getString("name"),
                                                rs.getInt(testServerEncryption ? "port_ssl" : "port"), null);
                                        if (ipv6 == null) {
                                            ((TestServerJson)server).setAddress(rs.getString("web_address"));
                                        } else if (ipv6) {
                                            ((TestServerJson)server).setAddress(rs.getString("web_address_ipv6"));
                                        } else {
                                            ((TestServerJson)server).setAddress(rs.getString("web_address_ipv4"));
                                        }
                                        logger.info("IP ADDRESS: " + ((TestServerJson)server).getAddress());
                                    }

                                    // close result set
                                    SQLHelper.closeResultSet(rs);

                                } catch (SQLException e) {
                                    logger.error(e.getMessage());
                                }

                            } else {
                                // get server
                                server = getNearestServer(geolat, geolong, clientIpString,
                                        asCountry, geoIpCountry, serverType, testServerEncryption, ipv6).get(0);

                                // get secret key
                                try (PreparedStatement ps = conn.prepareStatement(SELECT_SECRET_KEY_FOR_SERVER_ID)) {
                                    ps.setInt(1, ((TestServerJson)server).getId());
                                    logger.debug(ps.toString());
                                    ResultSet rs = ps.executeQuery();

                                    while (rs.next()) {
                                        secretKey = rs.getString("secret_key");
                                    }

                                    // close result set
                                    SQLHelper.closeResultSet(rs);

                                } catch (SQLException e) {
                                    logger.error(e.getMessage());
                                }
                            }

                            try {
                                if (server == null)
                                    throw new JSONException("could not find server");

                                if (timeZoneId.isEmpty()) {
                                    timeZoneId = Helperfunctions.getTimezoneId();
                                    timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
                                } else
                                    timeWithZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);

                                answer.put("test_server_address", ((TestServerJson)server).getAddress());
                                answer.put("test_server_port", ((TestServerJson)server).getPort());
                                answer.put("test_server_name", ((TestServerJson)server).getName());
                                answer.put("test_server_encryption", testServerEncryption);
                                //answer.put("measurement_server_id", server.getId());

                                answer.put("test_duration", getSetting("rmbt_duration"));
                                answer.put("test_numthreads", getSetting("rmbt_num_threads"));
                                answer.put("test_numpings", getSetting("rmbt_num_pings"));

                                answer.put("client_remote_ip", clientIpString);

                                Reference resultUrlReference = new Reference(getURL(), settings.getString("RMBT_RESULT_PATH"));
                                final String resultUrl = resultUrlReference.getTargetRef().toString();
                                logger.debug("resultUrl: " + resultUrl);
                                answer.put("result_url", resultUrl);


                                Reference resultQoSUrlReference = new Reference(getURL(), settings.getString("RMBT_QOS_RESULT_PATH"));
                                final String resultQoSUrl = resultQoSUrlReference.getTargetRef().toString();
                                logger.debug("resultQoSUrl: " + resultQoSUrl);
                                answer.put("result_qos_url", resultQoSUrl);

                            } catch (final JSONException e) {
                                logger.error("Error generating Answer " + e.toString());
                                errorList.addError("ERROR_RESPONSE_JSON");
                            }

                            if (errorList.getLength() == 0)
                                try {

                                    PreparedStatement st;
                                    st = conn.prepareStatement(INSERT_INTO_TEST, Statement.RETURN_GENERATED_KEYS);

                                    int i = 1;
                                    // uuid
                                    st.setObject(i++, UUID.fromString(testUuid));
                                    // open_test_uuid
                                    st.setObject(i++, UUID.fromString(testOpenUuid));
                                    // client_id
                                    st.setLong(i++, clientUid);
                                    // client_name
                                    st.setString(i++, clientName);
                                    // client_version
                                    st.setString(i++, clientVersion);
                                    // client_software_version
                                    st.setString(i++, request.optString("softwareVersion", null));
                                    // client_language
                                    st.setString(i++, lang);
                                    // client_public_ip
                                    st.setString(i++, clientIpString);
                                    // client_public_ip_anonymized
                                    st.setString(i++, Helperfunctions.anonymizeIp(clientAddress));
                                    // country_geoip (2digit country code derived from public IP of client)
                                    st.setString(i++, geoIpCountry);
                                    // server_id
                                    st.setInt(i++, ((TestServerJson)server).getId());
                                    // port
                                    st.setInt(i++, ((TestServerJson)server).getPort());
                                    // use_ssl
                                    st.setBoolean(i++, testServerEncryption);
                                    // timezone (of client)
                                    st.setString(i++, timeZoneId);
                                    // client_time (local time of client)
                                    st.setTimestamp(i++, clientTstamp, timeWithZone);
                                    // duration (requested)
                                    st.setInt(i++, Integer.parseInt(getSetting("rmbt_duration")));
                                    // num_threads_requested
                                    st.setInt(i++, Integer.parseInt(getSetting("rmbt_num_threads")));
                                    // status (of test)
                                    st.setString(i++, "STARTED"); // was "RUNNING" before
                                    // software_revision (of client)
                                    st.setString(i++, request.optString("softwareRevision", null));
                                    // client_test_counter (number of tests the client has performed)
                                    final int testCounter = request.optInt("testCounter", -1);
                                    if (testCounter == -1) // older clients did not support testCounter
                                        st.setNull(i++, Types.INTEGER);
                                    else
                                        st.setLong(i++, testCounter);
                                    // client_previous_test_status (outcome of previous test)
                                    st.setString(i++, request.optString("previousTestStatus", null));
                                    // AS name
                                    if (asn == null)
                                        st.setNull(i++, Types.BIGINT);
                                    else
                                        st.setLong(i++, asn);
                                    if (asName == null)
                                        st.setNull(i++, Types.VARCHAR);
                                    else
                                        st.setString(i++, asName);
                                    // AS country
                                    if (asCountry == null)
                                        st.setNull(i++, Types.VARCHAR);
                                    else
                                        st.setString(i++, asCountry);
                                    // public_ip_rdns
                                    String reverseDNS = Helperfunctions.reverseDNSLookup(clientAddress);
                                    if (reverseDNS == null || reverseDNS.isEmpty())
                                        st.setNull(i++, Types.VARCHAR);
                                    else {
                                        reverseDNS = reverseDNS.replaceFirst("\\.$", "");
                                        st.setString(i++, reverseDNS); // cut off last dot (#332)
                                    }
                                    // run_ndt
                                    if (request.has("ndt"))
                                        st.setBoolean(i++, request.getBoolean("ndt"));
                                    else
                                        st.setNull(i++, Types.BOOLEAN);

                                    logger.debug(st.toString());
                                    final int affectedRows = st.executeUpdate();
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_STORE_TEST");
                                    else {
                                        long key = 0;
                                        final ResultSet rs = st.getGeneratedKeys();
                                        if (rs.next())
                                            // Retrieve the auto generated
                                            // key(s).
                                            key = rs.getLong(1);

                                        // close result set
                                        SQLHelper.closeResultSet(rs);

                                        final PreparedStatement getProviderSt =
                                                conn.prepareStatement(SELECT_PROVIDER);
                                        getProviderSt.setLong(1, key);
                                        String provider = null;
                                        logger.debug(getProviderSt.toString());
                                        if (getProviderSt.execute()) {
                                            final ResultSet rs2 = getProviderSt.getResultSet();
                                            if (rs2.next())
                                                provider = rs2.getString(1);

                                            // close result set
                                            SQLHelper.closeResultSet(rs2);
                                        }
                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(getProviderSt);

                                        if (provider != null)
                                            answer.put("provider", provider);

                                        final PreparedStatement testSlotStatement =
                                                conn.prepareStatement(SELECT_NEXT_TEST_SLOT);
                                        testSlotStatement.setLong(1, key);
                                        int testSlot = -1;
                                        logger.debug(testSlotStatement.toString());
                                        if (testSlotStatement.execute()) {
                                            final ResultSet rs2 = testSlotStatement.getResultSet();
                                            if (rs2.next())
                                                testSlot = rs2.getInt(1);

                                            // close result set
                                            SQLHelper.closeResultSet(rs2);

                                            // close prepared statement
                                            SQLHelper.closePreparedStatement(testSlotStatement);
                                        }

                                        if (testSlot < 0)
                                            errorList.addError("ERROR_DB_STORE_GENERAL");
                                        else {
                                            final String data = testUuid + "_" + testSlot;
//                                            if (secretKey == null || secretKey.isEmpty()) {
//                                                secretKey = defaultSecretKey;
//                                            }
                                            final String hmac = Helperfunctions.calculateHMAC(secretKey, data);
                                            if (hmac.length() == 0)
                                                errorList.addError("ERROR_TEST_TOKEN");
                                            final String token = data + "_" + hmac;

                                            final PreparedStatement updateSt =
                                                    conn.prepareStatement(UPDATE_TOKEN);
                                            updateSt.setString(1, token);
                                            updateSt.setLong(2, key);
                                            logger.debug(updateSt.toString());
                                            updateSt.executeUpdate();

                                            answer.put("test_token", token);

                                            logger.info("test uuid: " + testUuid);

                                            answer.put("test_uuid", testUuid);
                                            answer.put("test_id", key);

                                            final long now = System.currentTimeMillis();
                                            int wait = testSlot - (int) (now / 1000);
                                            if (wait < 0)
                                                wait = 0;

                                            answer.put("test_wait", wait);

                                            if (geotime != 0 && geolat != 0 && geolong != 0) {

                                                final GeoLocation clientLocation = new GeoLocation(conn);

                                                clientLocation.setTest_id(key);

                                                final Timestamp geotstamp =
                                                        java.sql.Timestamp.valueOf(new Timestamp(geotime).toString());
                                                clientLocation.setTime(geotstamp, timeZoneId);

                                                clientLocation.setAccuracy(geoaccuracy);
                                                clientLocation.setAltitude(geoaltitude);
                                                clientLocation.setBearing(geobearing);
                                                clientLocation.setSpeed(geospeed);
                                                clientLocation.setProvider(geoprovider);
                                                clientLocation.setGeo_lat(geolat);
                                                clientLocation.setGeo_long(geolong);

                                                clientLocation.storeLocation();

                                                if (clientLocation.hasError())
                                                    errorList.addError(clientLocation.getError());
                                            }
                                        }
                                    }

                                    // close prepared statement
                                    SQLHelper.closePreparedStatement(st);

                                } catch (final SQLException e) {
                                    errorList.addError("ERROR_DB_STORE_GENERAL");
                                    logger.error(e.getMessage());

                                }

                        } else
                            errorList.addError("ERROR_CLIENT_UUID");

                    } else
                        errorList.addError("ERROR_CLIENT_VERSION");

                } else
                    errorList.addError("ERROR_DB_CONNECTION");
                // System.out.println(answer.toString(4));
            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSDON Data " + e.toString());
            }
        else
            errorList.addErrorString("Expected request is missing.");

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        answerString = answer.toString();
        long elapsedTime = System.currentTimeMillis() - startTime;
        logger.info("Test request from " + clientIpRaw + " completed " + Long.toString(elapsedTime) + " ms");

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }
}
