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
import java.util.Iterator;

public class LoadBadgesResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(LoadBadgesResource.class);

    public static final String SQL_SELECT_BADGES = "SELECT badge_id, title, description FROM badge_description where language = ?";
    public static final String SQL_UPDATE_TITLE = "UPDATE badge_description SET title = ? WHERE language = ? AND badge_id = ?";
    public static final String SQL_UPDATE_DESCRIPTION = "UPDATE badge_description SET description = ? WHERE language = ? AND badge_id = ?";

    private boolean performUpdate(PreparedStatement psUpdateDescription, String value, String language, long enId, ErrorList errorList) throws SQLException {
        psUpdateDescription.setString(1, value);
        psUpdateDescription.setString(2, language);
        psUpdateDescription.setLong(3, enId);
        int result = psUpdateDescription.executeUpdate();
        if (result != 1) {
            logger.error("Error updating data. : Unexpected number of rows was updated:" + result);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            errorList.addError("ERROR_REQUEST_JSON");
//            logger.error("rsponse: " + answer.toString());
            return false;
        }
        return true;
    }

    // Update data according the given JSON
    @Post("json")
    public String request(final String entity) {
        logger.debug("rquest badgeResourceOptions: " + entity);

        addAllowOrigin();

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String clientIpRaw = getIP();
        String language;

        logger.debug("Log request from: " + clientIpRaw);

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement psUpdateTitle = conn.prepareStatement(SQL_UPDATE_TITLE);
             PreparedStatement psUpdateDescription = conn.prepareStatement(SQL_UPDATE_DESCRIPTION);
        ) {

            JSONObject request;
            JSONObject translatedData;

            try {
                request = new JSONObject(entity);
                language = request.getString("language");
                translatedData = request.getJSONObject("translations");
            } catch (JSONException e) {
                logger.error("Error parsing JSDON Data: " + e.toString());
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                errorList.addError("ERROR_REQUEST_JSON");
                answer.putOpt("error", errorList.getList());
                logger.error("rsponse: " + answer.toString());
                return answer.toString();
            }

            for (Iterator<String> iter = translatedData.keys(); iter.hasNext(); ) {
                //answer.put("Key:", iter.next());
                String key = iter.next();
                String [] parsedKey = key.split("_");
                JSONObject translatedEntity = translatedData.getJSONObject(key);
                if (parsedKey.length != 3) {
                    logger.error("Error parsing JSDON Data. Key: \"" + key +"\" is not in expected format badge_KEYTYPE_ENGLISHID");
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    errorList.addError("ERROR_REQUEST_JSON");
                    answer.putOpt("error", errorList.getList());
                    logger.error("rsponse: " + answer.toString());
                    return answer.toString();
                }

                if (parsedKey[1].equals("description")) {
                    if (!performUpdate(psUpdateDescription, translatedEntity.get("message").toString(), language, java.lang.Long.parseLong(parsedKey[2]), errorList)) {
                        answer.putOpt("error", errorList.getList());
                        return answer.toString();
                    }
                } else if (parsedKey[1].equals("title")) {
                    if (!performUpdate(psUpdateTitle, translatedEntity.get("message").toString(), language, java.lang.Long.parseLong(parsedKey[2]), errorList)) {
                        answer.putOpt("error", errorList.getList());
                        return answer.toString();
                    }
                } else {
                    logger.error("Error parsing JSDON Data. Incorrect type of key: \"" + parsedKey[1] +"\". Expected \"description\" or \"title\"");
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    errorList.addError("ERROR_REQUEST_JSON");
                    answer.putOpt("error", errorList.getList());
                    logger.error("rsponse: " + answer.toString());
                    return answer.toString();
                }
            }

        } catch (NamingException ne) {
            errorList.addError("ERROR_DB_CONNECTION");
            logger.debug("DB connection is null");
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            answer.putOpt("error", errorList.getList());
            return answer.toString();
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

    //Generate JSON for Transifex
    @Get("json")
    public String retrieve(final String entity) {

        logger.debug("rquest badgeResourceOptions: " + entity);

        addAllowOrigin();

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String clientIpRaw = getIP();
        //String language;

        logger.debug("Log request from: " + clientIpRaw);

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement psBadges = conn.prepareStatement(SQL_SELECT_BADGES)) {

            psBadges.setString(1, "en");

            JSONObject keys = new JSONObject();

            try (ResultSet rsBadges = psBadges.executeQuery()) {

                while (rsBadges.next()) {

                    JSONObject badge = new JSONObject();
                    badge.put("message", rsBadges.getString(2));
                    badge.put("description", "");
                    keys.put("badge_title_" + rsBadges.getString(1), badge);

                    badge = new JSONObject();
                    badge.put("message", rsBadges.getString(3));
                    badge.put("description", "");
                    keys.put("badge_description_" + rsBadges.getString(1), badge);
                }
            }
            answer.put("badges:", keys);
        } catch (NamingException ne) {
            errorList.addError("ERROR_DB_CONNECTION");
            logger.debug("DB connection is null");
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            answer.putOpt("error", errorList.getList());
            return answer.toString();
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
}
