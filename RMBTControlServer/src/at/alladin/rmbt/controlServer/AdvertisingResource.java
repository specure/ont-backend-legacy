package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.SQLHelper;
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

public final class AdvertisingResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(AdvertisingResource.class);

    private final String SQL_SELECT_ADVERTISING = "SELECT adprovider, bannerid, appid FROM advertising WHERE country = ? AND active is TRUE ORDER BY uid DESC";
    private final String SQL_SELECT_ADVERTISING_FOR_ALL = "SELECT adprovider, bannerid, appid FROM advertising WHERE country = 'all' AND active is TRUE ORDER BY uid DESC";
    private final String SQL_SELECT_CLIENT = "SELECT * FROM client WHERE uuid = ?";

    @Post("json")
    public String request(final String entity) {

        logger.debug("rquest: " +entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        answer.put("isShowAdvertising", Boolean.FALSE);
        JSONArray jsonArray = new JSONArray();
        String uuidString = null;
        UUID client_uuid = null;
        String countryCode = null;

        String clientIpRaw = getIP();

        logger.debug("Log request from " + clientIpRaw);

        // parse JSON
        if (entity != null && !entity.isEmpty()) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                request = new JSONObject(entity);
                uuidString = request.getString("uuid");

                // check client UUID
                try {
                    if (uuidString.length() != 0) {
                        client_uuid = UUID.fromString(uuidString);
                    }
                } catch (IllegalArgumentException iae) {
                    errorList.addErrorString("Invalid  client UUID");
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    answer.put("error", errorList.getList());
                    logger.error(answer.toString());
                    return answer.toString();
                }

                // CHECK country code
                countryCode = request.getString("country");

                Connection conn = null;
                try {
                    conn = DbConnection.getConnection();
                } catch (NamingException e) {
                    logger.error(e.getMessage());
                }
                if (conn != null) {
                    // check if UUID is in DB
                    ps = conn.prepareStatement(SQL_SELECT_CLIENT);
                    ps.setObject(1, client_uuid);

                    logger.debug(ps.toString());
                    rs = ps.executeQuery();

                    if (!rs.next()) {
                        logger.debug("Client uuid is not in DB");
                        errorList.addErrorString("Client uuid is not in DB");
                        answer.putOpt("error", "Client uuid is not in DB.");
                        getResponse().setStatus(new Status(406));
                    }

                    SQLHelper.closeResultSet(rs);
                    SQLHelper.closePreparedStatement(ps);
                    //SQLHelper.closeConnection(conn);

                    // select advertising
                    ps = conn.prepareStatement(SQL_SELECT_ADVERTISING);
                    SQLHelper.setStringOrNull(ps, 1, countryCode);

                    //log
                    logger.debug(ps.toString());
                    rs = ps.executeQuery();

                    // check result set
                    if (rs.next()) {
                        answer.put("isShowAdvertising", Boolean.TRUE);
                        answer.put("adProvider", rs.getString(1));
                        answer.put("bannerId", rs.getString(2));
                        answer.put("appId", rs.getString(3));
                    } else {
                        // check country_code "all"
                        ps = conn.prepareStatement(SQL_SELECT_ADVERTISING_FOR_ALL);
                        //log
                        logger.debug(ps.toString());
                        rs = ps.executeQuery();

                        // check result set
                        if (rs.next()) {
                            answer.put("isShowAdvertising", Boolean.TRUE);
                            answer.put("adProvider", rs.getString(1));
                            answer.put("bannerId", rs.getString(2));
                            answer.put("appId", rs.getString(3));
                        } else {
                            // no data
                            answer.put("isShowAdvertising", Boolean.FALSE);
                        }
                    }

                    SQLHelper.closeResultSet(rs);
                    SQLHelper.closePreparedStatement(ps);
                    SQLHelper.closeConnection(conn);

                    getResponse().setStatus(Status.SUCCESS_OK);

                } else {
                    errorList.addError("ERROR_DB_CONNECTION");
                    logger.debug("DB connection is null");
                    getResponse().setStatus(new Status(500));
                    answer.putOpt("error", errorList.getList());
                    return answer.toString();
                }
            } catch (JSONException jse) {
                logger.error(jse.getMessage());
            } catch (SQLException se) {
                logger.error(se.getMessage());
            } finally {
                SQLHelper.closeResultSet(rs);
                SQLHelper.closePreparedStatement(ps);
                SQLHelper.closeConnection(conn);
            }
        }

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}
