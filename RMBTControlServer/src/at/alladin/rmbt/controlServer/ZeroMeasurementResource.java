package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.json.CellLocationJson;
import at.alladin.rmbt.shared.json.LocationJson;
import at.alladin.rmbt.shared.json.SignalJson;
import at.alladin.rmbt.shared.json.ZeroMeasurementJson;
import com.google.gson.*;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ZeroMeasurementResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ZeroMeasurementResource.class);

    // json request key
    private static final String KEY_ZERO_MEASUREMENT = "zero_measurement";

    // json response key
    private static final String KEY_SUCCESS = "success";

    // sql insert into test
    private static final String INSERT_ZERO_MEASUREMENTS_INTO_TEST = "INSERT INTO test(uuid, client_id, client_name, client_version, client_language, " +
            "time, timezone, plattform, product, api_level, network_operator, client_software_version, network_is_roaming, os_version, network_country, network_type," +
            " network_operator_name, network_sim_operator_name, model, network_sim_operator, device, phone_type, data_state, network_sim_country, status, " +
            "client_previous_test_status, implausible, zero_measurement, speed_upload, speed_download, ping_median, open_test_uuid) " +
            " VALUES( uuid_generate_v4(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, uuid_generate_v4())";

    // sql update test
    private static final String UPDATE_lTE_IN_TEST = "UPDATE test SET lte_rsrp = ?, lte_rsrq = ?, ss_rsrp = ? where uid = ?";

    // sql update test
    private static final String UPDATE_GEO_LOCATION_IN_TEST = "UPDATE test SET geo_lat = ?, geo_long = ?, location = ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913), geo_accuracy = ? where uid = ?";

    // sql insert into geo_location
    private static final String INSERT_INTO_GEO_LOCATIONS = "INSERT INTO geo_location(test_id, time, accuracy, altitude, bearing, speed, provider, geo_lat, geo_long, location, time_ns) " +
            " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ST_TRANSFORM(ST_SetSRID(ST_Point(?, ?), 4326), 900913), ?)";

    // sql insert into cell_location
    private static final String INSERT_INTO_CELL_LOCATIONS = "INSERT INTO cell_location(test_id, time, location_id, area_code, primary_scrambling_code, time_ns) " +
            " VALUES( ?, ?, ?, ?, ?, ?)";

    // sql insert into signal
    private static final String INSERT_INTO_SIGNAL = "INSERT INTO signal(test_id, time, network_type_id, lte_rsrp, lte_rsrq, lte_rssnr, lte_cqi, signal_strength, gsm_bit_error_rate, time_ns, ss_rsrp) " +
            " VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // sql select client id for client uuid
    private static final String SELECT_CLIENT_ID_FOR_CLIENT_UUID = "SELECT client.uid FROM client, client_type WHERE client.client_type_id = client_type.uid AND client.uuid = CAST ( ? AS uuid)";

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        // error list
        final ErrorList errorList = new ErrorList();

        // set default locales
        // get default language
        final String language = settings.getString("RMBT_DEFAULT_LANGUAGE");

        // get all supported languages
        final List<String> languages = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

        // check if default language is supported
        if (languages.contains(language)) {
            // is supported
            errorList.setLanguage(language);
            // set default locale
            labels = ResourceManager.getSysMsgBundle(new Locale(language));
        }

        // json to return
        final JsonObject answer = new JsonObject();

        // boolean to remember the previous value of autocommit
        boolean previousValueOfAutoCommit = true;

        // check request
        if (entity != null && !entity.isEmpty()) {
            // correct request

            // try parse the string to a JSON
            try {

                // get zero measurements
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement zeroMeasurementElement = gson.fromJson(entity, JsonElement.class);
                JsonArray zeroMeasurementArray = zeroMeasurementElement.getAsJsonObject().get(KEY_ZERO_MEASUREMENT).getAsJsonArray();
                List<ZeroMeasurementJson> zeroMeasurementList = Arrays.asList(gson.fromJson(zeroMeasurementArray, ZeroMeasurementJson[].class));

                // check count of zero measurements
                if (zeroMeasurementList != null && zeroMeasurementList.isEmpty() == false) {
                    // got some zero measurements

                    // check db connection
                    if (conn != null) {
                        // connection is ok

                        // flag indicates if we can continue with storing results
                        boolean doAbort = false;

                        // remember previous value of autocommit
                        previousValueOfAutoCommit = conn.getAutoCommit();

                        // turn off autocommit
                        conn.setAutoCommit(false);

                        // prepared statement
                        PreparedStatement preparedStatement;

                        // process each zero measurement
                        for (ZeroMeasurementJson zeroMeasurement : zeroMeasurementList) {

                            // check whether we can continue with storing data into database
                            if (doAbort) break;

                            // prepare statement
                            preparedStatement = conn.prepareStatement(INSERT_ZERO_MEASUREMENTS_INTO_TEST, Statement.RETURN_GENERATED_KEYS);

                            //insert into test
                            // uuid, client_id, client_name, client_version, client_language, time, timezone, plattform, product,
                            // api_level, network_operator, client_software_version, network_is_roaming, os_version,
                            // network_country, network_type, network_operator_name, network_sim_operator_name, model,
                            // network_sim_operator, device, phone_type, data_state, network_sim_country, status,
                            // client_previous_test_status, implausible, zero_measurement
                            int i = 1;

                            // uuid, do not set, will be generated by postgres method uuid_generate_v4()
                            //preparedStatement.setObject(i++, UUID.randomUUID());

                            // client_id
                            Long clientID = getClientByUuid(UUID.fromString(zeroMeasurement.getClient_uuid()));
                            if (clientID == null) {
                                // no client found for given uuid
                                logger.debug("Could not find client in database.");
                                errorList.addError("ERROR_DB_GET_CLIENT");
                                doAbort = true;
                                break;
                            }
                            preparedStatement.setLong(i++, clientID);

                            // client_name
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getClient_name());

                            // client_version
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getClient_version());

                            // client_language
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getClient_language());

                            // time
                            SQLHelper.setTimestampOrNull(preparedStatement, i++, zeroMeasurement.getTime());

                            // timezone
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTimezone());

                            // platform
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getPlattform());

                            // product
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getProduct());

                            // api_level
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getApi_level());

                            // telephony_network_operator
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_operator());

                            // client_software_version
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getClient_software_version());

                            // telephony_network_is_roaming
                            Boolean networkIsRoaming = Boolean.valueOf(zeroMeasurement.getTelephony_network_is_roaming());
                            SQLHelper.setBooleanOrNull(preparedStatement, i++, networkIsRoaming);

                            // os_version
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getOs_version());

                            // telephony_network_country
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_country());

                            // network_type
                            Integer networkType = null;
                            try {
                                networkType = Integer.valueOf(zeroMeasurement.getNetwork_type());
                            }catch (NumberFormatException ex) {
                            }
                            SQLHelper.setIntOrNull(preparedStatement, i++, networkType);

                            // telephony_network_operator_name
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_operator_name());

                            // telephony_network_sim_operator_name
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_sim_operator_name());

                            //  model
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getModel());

                            // telephony_network_sim_operator
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_sim_operator());

                            // device
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getDevice());

                            // telephony_phone_type": "1",
                            Integer phoneType = null;
                            try {
                                phoneType = Integer.valueOf(zeroMeasurement.getTelephony_phone_type());
                            } catch (NumberFormatException ex) {
                            }
                            SQLHelper.setIntOrNull(preparedStatement, i++, phoneType);

                            // telephony_data_state": "2",
                            Integer dataState = null;
                            try {
                                dataState = Integer.valueOf(zeroMeasurement.getTelephony_data_state());
                            } catch (NumberFormatException ex) {
                            }
                            SQLHelper.setIntOrNull(preparedStatement, i++, dataState);

                            // telephony_network_sim_country": "sk",
                            SQLHelper.setStringOrNull(preparedStatement, i++, zeroMeasurement.getTelephony_network_sim_country());

                            // status (of test)
                            preparedStatement.setString(i++, "FINISHED");

                            // client_previous_test_status (outcome of previous test)
                            preparedStatement.setString(i++, "END");

                            // implausible
                            preparedStatement.setBoolean(i++, Boolean.TRUE);

                            // new column zero_measurement
                            preparedStatement.setBoolean(i++, Boolean.TRUE);

                            // speed_download, set default value 0
                            preparedStatement.setInt(i++, 0);

                            // speed_upload, set default value 0
                            preparedStatement.setInt(i++, 0);

                            // ping_median, set default value max long
                            preparedStatement.setLong(i++, Long.MAX_VALUE);

                            // perform insert
                            logger.debug(preparedStatement.toString());
                            preparedStatement.execute();

                            // has to by generated during insert by postgres method uuid_generate_v4()
                            Long test_uuid = null;

                            // check resultSet and get generated test uuid
                            ResultSet rs = preparedStatement.getGeneratedKeys();
                            if (rs != null && rs.next()) {
                                // get test uuid
                                test_uuid = rs.getLong(1);

                                logger.debug("Test UUID returned from insert: " + test_uuid);

                                // close result set
                                SQLHelper.closeResultSet(rs);
                            }

                            // close prepared statement
                            SQLHelper.closePreparedStatement(preparedStatement);

                            // check generated test uuid
                            if (test_uuid != null && test_uuid > 0l) {
                                // insert ok

                                // insert into geo_location test_id, time, accuracy, altitude, bearing, speed, provider, geo_lat, geo_long, location, time_ns
                                // check geo locations
                                if (zeroMeasurement.getGeoLocations() != null && zeroMeasurement.getGeoLocations().isEmpty() == false) {

                                    for (LocationJson geoLocation : zeroMeasurement.getGeoLocations()) {

                                        // prepare statement
                                        preparedStatement = conn.prepareStatement(INSERT_INTO_GEO_LOCATIONS);

                                        i = 1;
                                        // test_id
                                        preparedStatement.setLong(i++, test_uuid);

                                        // time
                                        SQLHelper.setTimestampOrNull(preparedStatement, i++, geoLocation.getTstamp());

                                        // accuracy
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getAccuracy());

                                        // altitude
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getAltitude());

                                        // bearing
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getBearing());

                                        // speed
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getSpeed());

                                        // provider
                                        SQLHelper.setStringOrNull(preparedStatement, i++, geoLocation.getProvider());

                                        // geo_lat
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_lat());

                                        // geo_long
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_long());

                                        // location
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_long());
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_lat());

                                        // time_ns
                                        SQLHelper.setLongOrNull(preparedStatement, i++, geoLocation.getTime_ns());

                                        // store geo location
                                        logger.debug(preparedStatement.toString());
                                        preparedStatement.execute();

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(preparedStatement);

                                        // update geo location in table test
                                        i = 1;
                                        preparedStatement = conn.prepareStatement(UPDATE_GEO_LOCATION_IN_TEST);

                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_lat());
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_long());
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_long());
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getGeo_lat());

                                        // accuracy
                                        SQLHelper.setDoubleOrNull(preparedStatement, i++, geoLocation.getAccuracy());

                                        // where uid equals test_uuid
                                        preparedStatement.setLong(i++, test_uuid);

                                        // update test
                                        logger.debug(preparedStatement.toString());
                                        preparedStatement.execute();

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(preparedStatement);

                                    }// for

                                }//if check geo locations

                                // insert into cell_location test_id, time, location_id, area_code, primary_scrambling_code, time_ns
                                // check cell locations
                                if (zeroMeasurement.getCellLocations() != null && zeroMeasurement.getCellLocations().isEmpty() == false) {

                                    for (CellLocationJson cellLocationJson : zeroMeasurement.getCellLocations()) {

                                        // prepare statement
                                        preparedStatement = conn.prepareStatement(INSERT_INTO_CELL_LOCATIONS);

                                        i = 1;
                                        // test_id
                                        preparedStatement.setLong(i++, test_uuid);

                                        // time
                                        SQLHelper.setTimestampOrNull(preparedStatement, i++, cellLocationJson.getTime());

                                        // location_id
                                        SQLHelper.setIntOrNull(preparedStatement, i++, cellLocationJson.getLocation_id());

                                        // area_code
                                        SQLHelper.setIntOrNull(preparedStatement, i++, cellLocationJson.getArea_code());

                                        // primary_scrambling_code
                                        SQLHelper.setIntOrNull(preparedStatement, i++, cellLocationJson.getPrimary_scrambling_code());

                                        // time_ns
                                        SQLHelper.setLongOrNull(preparedStatement, i++, cellLocationJson.getTime_ns());

                                        // store geo location
                                        logger.debug(preparedStatement.toString());
                                        preparedStatement.execute();

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(preparedStatement);

                                    }// for
                                }//if check cell locations

                                // insert into signal test_id, time, network_type_id, lte_rsrp, lte_rsrq, lte_rssnr, lte_cqi, time_ns
                                // check signals
                                if (zeroMeasurement.getSignals() != null && zeroMeasurement.getSignals().isEmpty() == false) {

                                    for (SignalJson signalJson : zeroMeasurement.getSignals()) {

                                        // prepare statement
                                        preparedStatement = conn.prepareStatement(INSERT_INTO_SIGNAL);

                                        i = 1;

                                        // test_id
                                        preparedStatement.setLong(i++, test_uuid);

                                        // time
                                        SQLHelper.setTimestampOrNull(preparedStatement, i++, signalJson.getTime());

                                        // network_type_id
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getNetwork_type_id());

                                        // lte_rsrp
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_rsrp());

                                        // lte_rsrq
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_rsrq());

                                        // lte_rssnr
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_rssnr());

                                        // lte_cqi
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_cqi());

                                        // signal_strength
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getSignal_strength());

                                        // gsm_bit_error_rate
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getGsm_bit_error_rate());

                                        // time_ns
                                        SQLHelper.setLongOrNull(preparedStatement, i++, signalJson.getTime_ns());

                                        // ss_rsrp
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getSs_rsrp());

                                        // store signal and check result
                                        logger.debug(preparedStatement.toString());
                                        preparedStatement.execute();

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(preparedStatement);

                                        // update lte in table test
                                        i = 1;
                                        preparedStatement = conn.prepareStatement(UPDATE_lTE_IN_TEST);

                                        // lte_rsrp
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_rsrp());

                                        // lte_rsrq
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getLte_rsrq());

                                        // ss_rsrp
                                        SQLHelper.setIntOrNull(preparedStatement, i++, signalJson.getSs_rsrp());

                                        // where uid equals test_uuid
                                        preparedStatement.setLong(i++, test_uuid);

                                        // update test
                                        logger.debug(preparedStatement.toString());
                                        preparedStatement.execute();

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(preparedStatement);

                                    }// for
                                }//if check signals


                            } else {
                                // insert failed, add error to response
                                errorList.addError("ERROR_DB_STORE_GENERAL");
                                // abort storing
                                doAbort = true;
                            }// if uuid != null

                        }//for


                        // check errors, do commit / rollback, set state for return
                        if (doAbort || (errorList != null && errorList.isEmpty() == false)) {
                            // some error, do rollback, set the previous value of autocommit, return success=false

                            // do rollback and set the previous value of autocommit
                            doRollBackAndSetPreviousValueOfAutocommit(previousValueOfAutoCommit);

                            // return success=false
                            answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
                        } else {
                            // no error, do commit, set the previous value of autocommit, return success=true

                            // do commit
                            conn.commit();

                            // set the previous value of autocommit
                            conn.setAutoCommit(previousValueOfAutoCommit);

                            // return success=true
                            answer.addProperty(KEY_SUCCESS, Boolean.TRUE);
                        }// if

                    } else {
                        // no database connection

                        // add error to response
                        errorList.addError("ERROR_DB_CONNECTION");

                        // return success=false
                        answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
                    }// if conn != null

                } else {
                    // got empty list of zero measurements

                    // add error to response
                    errorList.addError("ERROR_REQUEST_JSON");

                    // return success=false
                    answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
                }//if


            } catch (JsonSyntaxException exJson) {
                logger.error("Error parsing JSDON Data " + exJson.toString());

                // add error to response
                errorList.addError("ERROR_REQUEST_JSON");

                // do rollback and set the previous value of autocommit
                doRollBackAndSetPreviousValueOfAutocommit(previousValueOfAutoCommit);

                // return success=false
                answer.addProperty(KEY_SUCCESS, Boolean.FALSE);

            } catch (SQLException exSql) {
                logger.error("Error occurred during storing zero measurements into database! Exception:  " + exSql.toString());

                // add error to response
                errorList.addError("ERROR_DB_STORE_GENERAL");

                // do rollback and set the previous value of autocommit
                doRollBackAndSetPreviousValueOfAutocommit(previousValueOfAutoCommit);

                // return success=false
                answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
            } catch (Exception ex) {
                logger.error("Error occurred during storing zero measurements into database! Exception:  " + ex.toString());

                // add error to response
                errorList.addError("ERROR_DB_STORE_GENERAL");

                // do rollback and set the previous value of autocommit
                doRollBackAndSetPreviousValueOfAutocommit(previousValueOfAutoCommit);

                // return success=false
                answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
            }// try

        } else {
            // invalid request
            logger.error("Illegal JSON request received.");

            // add error to response
            errorList.addError("ERROR_REQUEST_JSON");

            // return success=false
            answer.addProperty(KEY_SUCCESS, Boolean.FALSE);
        }// if

        // add error list to response
        answer.add("error", errorList.getListAsJsonArray());

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

    private void doRollBackAndSetPreviousValueOfAutocommit(boolean previousValueOfAutoCommit) {

        try {
            // rollback
            conn.rollback();

            // set the previous value of autocommit
            conn.setAutoCommit(previousValueOfAutoCommit);

        } catch (SQLException sqlEx) {
            logger.error("An error occurred during rollback! Exception: " + sqlEx.getMessage());
        }
    }

    private Long getClientByUuid(UUID uuid) {
        Long clientId = null;
        try {

            // prepare statement
            PreparedStatement st = conn.prepareStatement(SELECT_CLIENT_ID_FOR_CLIENT_UUID);
            st.setString(1, uuid.toString());

            // execute query
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                clientId = rs.getLong("uid");
            }

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return clientId;
    }

}
