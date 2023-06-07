/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NewsResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(NewsResource.class);

    private static final String SQL_SELECT_TEXT = "SELECT uid,title_%s" + " AS title, text_%s"
            + " AS text FROM news " + " WHERE" + " (uid > ? OR force = true)" + " AND active = true"
            + " AND (plattform IS NULL OR plattform = ?)"
            + " AND (max_software_version_code IS NULL OR ? <= max_software_version_code)"
            + " AND (min_software_version_code IS NULL OR ? >= min_software_version_code)"
            + " AND (uuid IS NULL OR uuid::TEXT = ?)" + // convert to text so that empty uuid-strings are tolerated
            " ORDER BY time ASC";

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        logger.info("New news request from " + getIP());

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                String lang = request.optString("language");

                // Load Language Files for Client

                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                } else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

                String sqlLang = lang;
                if (!sqlLang.equals("de"))
                    sqlLang = "en";

                if (conn != null) {
                    final long lastNewsUid = request.optLong("lastNewsUid");
                    final String plattform = request.optString("plattform");
                    final int softwareVersionCode = request.optInt("softwareVersionCode", -1);
                    String uuid = request.optString("uuid");

                    final JSONArray newsList = new JSONArray();

                    try {

//                        final PreparedStatement st = conn
//                                .prepareStatement("SELECT uid,title_" + sqlLang + 
//                                        " AS title, text_" + sqlLang +
//                                        " AS text FROM news " +
//                                        " WHERE" +
//                                        " (uid > ? OR force = true)" +
//                                        " AND active = true" +
//                                        " AND (plattform IS NULL OR plattform = ?)" +
//                                        " AND (max_software_version_code IS NULL OR ? <= max_software_version_code)" +
//                                        " AND (min_software_version_code IS NULL OR ? >= min_software_version_code)" +
//                                        " AND (uuid IS NULL OR uuid::TEXT = ?)" + //convert to text so that empty uuid-strings are tolerated
//                                        " ORDER BY time ASC");
                        final PreparedStatement st = conn
                                .prepareStatement(String.format(SQL_SELECT_TEXT, sqlLang, sqlLang));
                        st.setLong(1, lastNewsUid);
                        st.setString(2, plattform);
                        st.setInt(3, softwareVersionCode);
                        st.setInt(4, softwareVersionCode);
                        st.setString(5, uuid);

                        final ResultSet rs = st.executeQuery();

                        while (rs.next()) {
                            final JSONObject jsonItem = new JSONObject();

                            jsonItem.put("uid", rs.getInt("uid"));
                            jsonItem.put("title", rs.getString("title"));
                            jsonItem.put("text", rs.getString("text"));


                            newsList.put(jsonItem);
                        }
                        // close result set
                        SQLHelper.closeResultSet(rs);

                        // close prepared statement
                        SQLHelper.closePreparedStatement(st);

                    } catch (final SQLException e) {
                        logger.error(e.getMessage());
                        errorList.addError("ERROR_DB_GET_NEWS_SQL");
                    }
//                    }

                    answer.put("news", newsList);

                } else
                    errorList.addError("ERROR_DB_CONNECTION");

            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSON Data " + e.toString());
            }
        else
            errorList.addErrorString("Expected request is missing.");

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
