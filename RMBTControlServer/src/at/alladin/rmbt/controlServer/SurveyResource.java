package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.SQLHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.UUID;

/**
 * @author tomas.hreben
 * @email tomas.hreben@martes-specure.com
 * @date 25.1.2018
 */
public class SurveyResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(SurveyResource.class);

    private final String SQL_INSERT_SURVEY_RESULT = "INSERT INTO survey_result(client_uuid, email, time, questionnaire) " +
            "VALUES(?, ?, NOW(), ?::JSON)";

    private final String SQL_SELECT_CLIENT = "SELECT * FROM client WHERE uuid = ?";

    private final String SQL_SELECT_CLIENT_UUID = "SELECT * FROM survey_result WHERE client_uuid = ?";

    @Post("json")
    public String request(final String entity) {

        logger.debug("rquest: " +entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String email = null;
        JSONObject questionnaire = null;
        String uuidString = null;
        UUID client_uuid = null;

        String clientIpRaw = getIP();

        logger.debug("Log request from " + clientIpRaw);

//      parse JSON
        if (entity != null && !entity.isEmpty()) {
            try {
                request = new JSONObject(entity);

                questionnaire = request.getJSONObject("surveyData");

                uuidString = request.getString("client_uuid");

                if (uuidString.length() != 0) {
                    client_uuid = UUID.fromString(uuidString);
                }

                if (conn != null) {
//              check if UUID is in DB table client
                    PreparedStatement ps = conn.prepareStatement(SQL_SELECT_CLIENT);
                    ps.setObject(1, client_uuid);

                    logger.debug(ps.toString());
                    ResultSet rs = ps.executeQuery();

                    if (!rs.next()) {
                        logger.debug("Client uuid is not in DB");
                        errorList.addErrorString("Client uuid is not in DB");
                        answer.putOpt("error", errorList.getList());
                        getResponse().setStatus(new Status(406));
                        return answer.toString();
                    }

//                  check if exist survey with this client_uuid
//                  turn off check based email from Jozef Svrcek Jr. - 26.2.2018
/**                    ps = conn.prepareStatement(SQL_SELECT_CLIENT_UUID);
                    ps.setObject(1, client_uuid);

                    logger.debug(ps.toString());
                    rs = ps.executeQuery();

                    if (rs.next()) {
                        logger.debug("Client has completed the questionnaire ");
                        errorList.addErrorString("Client has completed the questionnaire");
                        answer.putOpt("error", errorList.getList());
                        getResponse().setStatus(new Status(406));
                        return answer.toString();
                    }
 **/

                    SQLHelper.closeResultSet(rs);

                    try {
                        email = request.getString("email");
                    } catch (JSONException e) {
                        // do nothing, email is optional
                    }

                    if (email != null && email.isEmpty() == false && !Helperfunctions.isEmailValid(email)) {
//                    email is not valid
                        getResponse().setStatus(new Status(422));
                        errorList.addError("ERROR_REQUEST_JSON");
                        answer.putOpt("error", "Email is not valid.");
                        return answer.toString();
                    }

                    ps = conn.prepareStatement(SQL_INSERT_SURVEY_RESULT);

                    ps.setObject(1, client_uuid);
                    if( email == null) {
                        ps.setNull(2, Types.VARCHAR);
                    } else {
                        ps.setObject(2, email);
                    }
                    ps.setObject(3, questionnaire.toString());

                    logger.debug(ps.toString());
                    ps.execute();

                    SQLHelper.closePreparedStatement(ps);

                } else {
                    errorList.addError("ERROR_DB_CONNECTION");
                    logger.debug("DB connection is null");
                    getResponse().setStatus(new Status(500));
                    answer.putOpt("error", errorList.getList());
                    return answer.toString();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
                errorList.addError("ERROR_DB_STORE_GENERAL");
            } catch (JSONException je) {
                logger.error("Error parsing JSDON Data " + je.getMessage());
                errorList.addError("ERROR_REQUEST_JSON");
            } catch (IllegalArgumentException iae) {
                logger.error("UUID: " + iae.getMessage());
                errorList.addError("ERROR_CLIENT_UUID");
            }
        }


        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        if (!errorList.isEmpty()) {
            getResponse().setStatus(new Status(406));
        } else {
            answer.put("status", "OK");
            getResponse().setStatus(new Status(200));
        }


        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }

}
