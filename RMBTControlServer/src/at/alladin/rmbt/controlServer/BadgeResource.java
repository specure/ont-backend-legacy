package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.ErrorList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BadgeResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(BadgeResource.class);

    public static final String SQL_SELECT_BADGES = "SELECT b.id AS id, COALESCE (bd.title, bd_en.title) AS title, " +
            " COALESCE (bd.description, bd_en.description) AS description, b.category AS category, " +
            " b.terms_operator AS terms_operator, b.phone_id AS phone_id, b.image_link as image_link " +
            " FROM badge b " +
            " LEFT JOIN badge_description bd ON b.id=bd.badge_id AND bd.language= ? " +
            " LEFT JOIN badge_description bd_en ON b.id=bd_en.badge_id AND bd_en.language='en'";

    public static final String SQL_SELECT_CRITERIA =
            "SELECT id, type, operator, value FROM badge_criteria where badge_id = ?";

    /*
        This entry points returns a list of badges for given language
        Input:
        {
          "language": "en"
        }

        Output:
        {
          "badges": [
            {
              "id": "badge_id01",
              "title": "ROOKIE",
              "description": "You will get this badge for performing at least 6 measurements",
              "category": "measurement",
              "image_link": "https://nettest.org/badges/rookie.png",
              "criteria": {
                "terms_operator": "and",
                "terms": [
                  {
                    "type": "measurement",
                    "operator": "ge",
                    "value": "6"
                  }
                ]
              }
            },
            {
              "id": "badge_id02",
              "title": "CHRISTMAS EVE",
              "description": "You will get this badge for performing measurement at Christmas Eve",
              "category": "holiday",
              "image_link": "https://nettest.org/badges/christmas_eve.png",
              "criteria": {
                "terms_operator": "and",
                "terms": [
                  {
                    "type": "date",
                    "operator": "eq",
                    "value": "24.12"
                  }
                ]
              }
            },
            {
              "id": "badge_id03",
              "title": "EASTER WEEK",
              "description": "You will get this badge for performing measurement during the Easter",
              "category": "holiday",
              "image_link": "https://nettest.org/badges/easter_monday.png",
              "criteria": {
                "terms_operator": "and",
                "terms": [
                  {
                    "type": "exactdate",
                    "operator": "ge",
                    "value": "03.04.2019"
                  },
                  {
                    "type": "exactdate",
                    "operator": "le",
                    "value": "06.04.2019"
                  }
                ]
              }
            }
          ]
        }
     */


    private void fillBadges(String language, PreparedStatement psBadges, PreparedStatement psCriteria, JSONArray badges) throws SQLException {
        psBadges.setString(1, language);

        try (ResultSet rsBadges = psBadges.executeQuery()) {


            while (rsBadges.next()) {

                JSONObject badge = new JSONObject();
                JSONArray badgeCriteriaArray = new JSONArray();
                badge.put("id", rsBadges.getString("phone_id"));
                badge.put("title", rsBadges.getString("title"));
                badge.put("description", rsBadges.getString("description"));
                badge.put("category", rsBadges.getString("category"));
                badge.put("image_link", rsBadges.getString("image_link"));
                badge.put("terms_operator", rsBadges.getString("terms_operator"));

                // to search for badge criteria we use english language id (value 8)
                // so we have only one badge criteria for one badge, regardless on how many languages we have
                psCriteria.setLong(1, rsBadges.getLong("id"));
                try (ResultSet rsCriteria = psCriteria.executeQuery()) {
                    JSONObject badgeCriteria = new JSONObject();

                    while (rsCriteria.next()) {
                        badgeCriteria = new JSONObject();
                        badgeCriteria.put("type", rsCriteria.getString("type"));
                        badgeCriteria.put("operator", rsCriteria.getString("operator"));
                        badgeCriteria.put("value", rsCriteria.getString("value"));
                        badgeCriteriaArray.put(badgeCriteria);
                    }
                }
                badge.put("criteria", badgeCriteriaArray);
                badges.put(badge);
            }
        }
    }

    @Post("json")
    public String retrieve(final String entity) {

        logger.debug("rquest badgeResourceOptions: " + entity);

        addAllowOrigin();

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String clientIpRaw = getIP();
        String language;

        logger.debug("Log request from: " + clientIpRaw);

        JSONObject request;
        try {
            request = new JSONObject(entity);
            language = request.getString("language");
        } catch (JSONException e) {
            logger.error("Error parsing JSDON Data: " + e.toString());
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            errorList.addError("ERROR_REQUEST_JSON");
            answer.putOpt("error", errorList.getList());
            logger.error("rsponse: " + answer.toString());
            return answer.toString();
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement psBadges = conn.prepareStatement(SQL_SELECT_BADGES);
             PreparedStatement psCriteria = conn.prepareStatement(SQL_SELECT_CRITERIA)) {

            final JSONArray badges = new JSONArray();

            fillBadges(language, psBadges, psCriteria, badges);

            answer.put("badges", badges);
            getResponse().setStatus(Status.SUCCESS_OK);


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
