package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// this service allows to request a list of options for extra info about the speed of a fixed line
// and send these extra data when saving measurement

public class AdvertisedSpeedResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(AdvertisedSpeedResource.class);

    private final String SQL_SELECT_ADVERTISED_SPEED = "SELECT uid, name, min_speed_down_kbps, max_speed_down_kbps, min_speed_up_kbps, max_speed_up_kbps FROM advertised_speed_option WHERE enabled order by uid asc";
    private final String SQL_INSERT_ADVERTISED_SPEED = "INSERT INTO advertised_speed (test_uid, advertised_speed_option_uid, speed_down_kbps, speed_up_kbps) VALUES (?, ?, ?, ?)";
    private final String SQL_SELECT_CLIENT = "SELECT uid FROM client WHERE uuid = ?";
    private final String SQL_SELECT_TEST_UID = "SELECT uid FROM test WHERE uuid = ?";

    // this entry point sends a possible options for the Advertised Speed Info data to choose from by the user
    // INPUT: - nothing
    // OUTPUT: json data in the form:
    // {
    //   "advertisedSpeedOptions":[
    //      {
    //         "uid":1,
    //         "max_speed_up_kbps":"100000",
    //         "max_speed_down_kbps":"100000",
    //         "name":"xDSL",
    //         "min_speed_up_kbps":"0",
    //         "min_speed_down_kbps":"0"
    //      },
    //      {
    //         "uid":2,
    //         "max_speed_up_kbps":"1000000",
    //         "max_speed_down_kbps":"1000000",
    //         "name":"Ethernet",
    //         "min_speed_up_kbps":"0",
    //         "min_speed_down_kbps":"0"
    //      },
    //      {
    //         "uid":3,
    //         "max_speed_up_kbps":"108000",
    //         "max_speed_down_kbps":"400000",
    //         "name":"DOCSIS",
    //         "min_speed_up_kbps":"0",
    //         "min_speed_down_kbps":"0"
    //      }
    //   ]
    // }

    @Get("json")
    public String retrieve(final String entity) {
        logger.debug("rquest advertisedSpeedOptions: " + entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        JSONArray advertisedSpeedInfoList = new JSONArray();
        String clientIpRaw = getIP();

        logger.debug("Log request from: " + clientIpRaw);

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ADVERTISED_SPEED);
             ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                JSONObject advertisedSpeedInfo = new JSONObject();
                advertisedSpeedInfo.put("uid", rs.getLong(1));
                advertisedSpeedInfo.put("name", rs.getString(2));
                advertisedSpeedInfo.put("min_speed_down_kbps", rs.getLong(3));
                advertisedSpeedInfo.put("max_speed_down_kbps", rs.getLong(4));
                advertisedSpeedInfo.put("min_speed_up_kbps", rs.getLong(5));
                advertisedSpeedInfo.put("max_speed_up_kbps", rs.getLong(6));

                advertisedSpeedInfoList.put(advertisedSpeedInfo);
            }

            answer.put("advertisedSpeedOptions", advertisedSpeedInfoList);

            getResponse().setStatus(Status.SUCCESS_OK);

        } catch (NamingException e) {
            logger.error(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addErrorString("Internal server error - JSON.");
            answer.putOpt("error", errorList.getList());
        } catch (JSONException jse) {
            logger.error(jse.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addErrorString("Internal server error - JSON.");
            answer.putOpt("error", errorList.getList());
        } catch (SQLException se) {
            logger.error(se.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addErrorString("Internal server error - SQL.");
            answer.putOpt("error", errorList.getList());
        }
        logger.debug("rsponse: " + answer.toString());
        return answer.toString();
    }

    // helper method
    // checks, whether given string is a correct UUID
    // if yes, returns it as UUID object, otherwise returns null
    private UUID checkUUID(String uuid, ErrorList errorList, JSONObject answer, String errorMessage) {
        UUID result = null;
        try {
            if (uuid.length() != 0) {
                result = UUID.fromString(uuid);
            }
        } catch (IllegalArgumentException iae) {
            errorList.addErrorString(errorMessage);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            answer.put("error", errorList.getList());
            logger.error(answer.toString());
        }

        return result;
    }

    // helper method
    // Executes a SELECT query and checks, whether there is at least one record
    // returns uid of the row if it exists, otherwise null
    private Long checkRecordExistence(PreparedStatement ps, ErrorList errorList, JSONObject answer, String errorMessage) throws SQLException {

        Long result = null;
        logger.debug(ps.toString());
        try (ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                logger.debug(errorMessage);
                errorList.addErrorString(errorMessage);
                answer.putOpt("error", errorList.getList());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            } else {
                result = rs.getLong(1);
            }
        }
        return result;
    }


    // this entry point stored an Advertised Speed info for a given measurement into DB
    // INPUT: json in the form:
    // OUTPUT: result of the operation, either success of failure with explanation

    @Post("json")
    public String request(final String entity) {

        logger.debug("rquest: " + entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String clientUuidString = null;
        String testUuidString = null;
        UUID client_uuid = null;
        UUID test_uuid = null;

        String clientIpRaw = getIP();
        Long test_uid, advertised_speed_option_uid, speed_down_kbps, speed_up_kbps;

        logger.debug("Log request from: " + clientIpRaw);

        // parse JSON
        if (entity != null && !entity.isEmpty()) {
            try (
                    Connection conn = DbConnection.getConnection();
                    PreparedStatement psCheckClient = conn.prepareStatement(SQL_SELECT_CLIENT);
                    PreparedStatement psCheckUUID = conn.prepareStatement(SQL_SELECT_TEST_UID);
                    PreparedStatement ps = conn.prepareStatement(SQL_INSERT_ADVERTISED_SPEED);
            ) {
                request = new JSONObject(entity);

                clientUuidString = request.getString("client_uuid");
                testUuidString = request.getString("test_uuid");
                advertised_speed_option_uid = request.getLong("advertised_speed_option_uid");
                speed_down_kbps = request.getLong("speed_down_kbps");
                speed_up_kbps = request.getLong("speed_up_kbps");

                if ((client_uuid = checkUUID(clientUuidString, errorList, answer, "Invalid  client UUID")) == null) {
                    return answer.toString();
                }

                if ((test_uuid = checkUUID(testUuidString, errorList, answer, "Invalid  test UUID")) == null) {
                    return answer.toString();
                }


                // check if Client UUID is in DB
                psCheckClient.setObject(1, client_uuid);

                if (checkRecordExistence(psCheckClient, errorList, answer, "Client does not exist in DB") == null) {
                    return answer.toString();
                }

                psCheckUUID.setObject(1, test_uuid);

                if ((test_uid = checkRecordExistence(psCheckUUID, errorList, answer, "Test uuid is not in DB")) == null) {
                    return answer.toString();
                }

                // select advertising
                ps.setLong(1, test_uid);
                ps.setLong(2, advertised_speed_option_uid);
                ps.setLong(3, speed_down_kbps);
                ps.setLong(4, speed_up_kbps);

                //log
                logger.debug(ps.toString());

                try {
                    if (ps.executeUpdate() != 1) {

                    }
                } catch (SQLException e) {
                    errorList.addErrorString("Error when inserting speed info data into database.");
                    logger.debug("Error when inserting speed data into database: " + e.toString());
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    answer.putOpt("error", errorList.getList());
                    return answer.toString();
                }

                getResponse().setStatus(Status.SUCCESS_OK);

            } catch (NamingException e) {
                logger.debug("DB connection is null");
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                answer.putOpt("error", errorList.getList());
                logger.error(e.getMessage());
                return answer.toString();
            } catch (JSONException jse) {
                logger.error(jse.getMessage());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                errorList.addErrorString("Internal server error - JSON.");
                answer.putOpt("error", errorList.getList());
            } catch (SQLException se) {
                logger.error(se.getMessage());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                errorList.addErrorString("Internal server error - SQL");
                answer.putOpt("error", errorList.getList());
            }

        }

        return answer.toString();
    }
}
