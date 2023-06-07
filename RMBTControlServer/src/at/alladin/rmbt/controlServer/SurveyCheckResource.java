package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.shared.ErrorList;
import com.google.common.net.InetAddresses;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @author tomas.hreben
 * @email tomas.hreben@martes-specure.com
 * @date 1. februar 2018
 */
public class SurveyCheckResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(SurveyCheckResource.class);

    private static final String SQL_CHECK_CLIENT_UUID = "SELECT * FROM survey_result WHERE client_uuid = ?";

    private final String SQL_SELECT_CLIENT = "SELECT * FROM client WHERE uuid = ?";

    @Post
    public String request(String entity) {

        logger.debug("rquest: " +entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        String uuidString = null;
        UUID client_uuid = null;
        String country_code_request = null;

        String clientIpRaw = getIP();

        logger.debug("Log request from " + clientIpRaw);

//        parse JSON
        if (entity != null && !entity.isEmpty()) {
            try {
                request = new JSONObject(entity);
                uuidString = request.getString("client_uuid");

                try {
                    if (uuidString.length() != 0) {
                        client_uuid = UUID.fromString(uuidString);
                    }
                } catch (IllegalArgumentException iae){
                    errorList.addErrorString("Invalid  client UUID");
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    answer.put("error",errorList.getList());
                    logger.error(answer.toString());
                    return answer.toString();
                }

                if (conn != null) {
//              check if UUID is in DB
                    PreparedStatement ps = conn.prepareStatement(SQL_SELECT_CLIENT);
                    ps.setObject(1, client_uuid);

                    logger.debug(ps.toString());
                    ResultSet rs = ps.executeQuery();

                    if (!rs.next()) {
                        logger.debug("Client uuid is not in DB");
                        errorList.addErrorString("Client uuid is not in DB");
                        answer.putOpt("error", "Client uuid is not in DB.");
                        getResponse().setStatus(new Status(406));
                    }

//                  check if user completed the survey
//                  turn off check based email from Jozef Svrcek Jr. - 26.2.2018
/**                    ps = conn.prepareStatement(SQL_CHECK_CLIENT_UUID);
                    ps.setObject(1, client_uuid);

                    logger.debug(ps.toString());
                    rs = ps.executeQuery();



                    if (rs.next()) {
                        logger.debug("Client completed the survey.");
                        JSONObject jsonItemSurvey = new JSONObject();
                        jsonItemSurvey.put("survey_url", getSetting("survey_url", null));
                        jsonItemSurvey.put("is_filled_up", true);
                        jsonArray.put(jsonItemSurvey);
                        answer.put("survey", jsonArray);
                        getResponse().setStatus(Status.SUCCESS_OK);
                    }else{
 **/
                        JSONObject jsonItemSurvey = new JSONObject();
                        jsonItemSurvey.put("survey_url", getSetting("survey_url", null));
                        jsonItemSurvey.put("is_filled_up", false);
                        jsonArray.put(jsonItemSurvey);
                        answer.put("survey", jsonArray);
                        getResponse().setStatus(Status.SUCCESS_OK);
//                    }

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
}
