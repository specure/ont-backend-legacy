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
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(SyncResource.class);

    private static final String SQL_CODE = "SELECT rmbt_get_sync_code(CAST (? AS UUID)) AS code";
    private static final String SQL_CLIENT = "SELECT * FROM client WHERE uuid = CAST(? AS UUID)";
    private static final String SQL_CLIENT_SYNC =
            "SELECT * FROM client WHERE sync_code = ? AND sync_code_timestamp + INTERVAL '1 month' > NOW()";
    private static final String SQL_INSERT_SYNC_GROUP =
            "INSERT INTO sync_group(tstamp) " + "VALUES(now())";
    private static final String SQL_UPDATE_CLIENT = "UPDATE client SET sync_group_id = ? WHERE uid = ? OR uid = ?";
    private static final String SQL_UPDATE_CLIENT_UID = "UPDATE client SET sync_group_id = ? WHERE uid = ?";
    private static final String SQL_UPDATE_CLIENT_SYNC_GROUP_UID = "UPDATE client SET sync_group_id = ? WHERE sync_group_id = ?";
    private static final String SQL_DELETE_SYNC_GROUP = "DELETE FROM sync_group WHERE uid = ?";

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        logger.info("New sync request from " + getIP());

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

//                System.out.println(request.toString(4));

                if (conn != null) {

                    final JSONArray syncList = new JSONArray();

                    UUID uuid = null;

                    try {
                        if (request.optString("uuid").length() > 0)
                            uuid = UUID.fromString(request.getString("uuid"));
                    } catch (IllegalArgumentException iae) {
                        errorList.addErrorString("Invalid  client UUID");
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        answer.put("error", errorList.getList());
                        logger.error(answer.toString());
                        return answer.toString();
                    }

//                  check if user uuid exit in DB table client
                    try {
                        PreparedStatement ps = conn.prepareStatement(SQL_CLIENT);
                        ps.setString(1, uuid.toString());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            errorList.addError("ERROR_DB_GET_CLIENT");
                            answer.putOpt("error", errorList.getList());
                            // log response
                            logger.debug("rsponse: " + answer.toString());

                            return answer.toString();
                        }

                    } catch (SQLException se) {
                        logger.error(se.getMessage());
                        errorList.addError("ERROR_DB_GET_SYNC_SQL");
                    }

                    if (uuid != null && request.optString("sync_code").length() == 0) {

                        String syncCode = "";

                        try {

                            final PreparedStatement st = conn
                                    .prepareStatement(SQL_CODE);
                            st.setString(1, uuid.toString());

                            final ResultSet rs = st.executeQuery();

                            if (rs.next())
                                syncCode = rs.getString("code");
                            else
                                errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                            // new Object[] {uuid}));

                            // close result set
                            SQLHelper.closeResultSet(rs);

                            // close prepared statement
                            SQLHelper.closePreparedStatement(st);

                        } catch (final SQLException e) {
                            logger.error(e.getMessage());
                            errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE);
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                        }

                        if (errorList.getLength() == 0) {
                            final JSONObject jsonItem = new JSONObject();
                            //lower case code is easier to enter on mobile devices
                            jsonItem.put("sync_code", syncCode.toLowerCase(Locale.US));

                            syncList.put(jsonItem);

                        }
                    } else if (uuid != null && checkSyncCode(request.optString("sync_code"), errorList)) {
                        final String syncCode = request.getString("sync_code").toUpperCase(Locale.US);
                        int syncGroup1 = 0;
                        int uid1 = 0;
                        int syncGroup2 = 0;
                        int uid2 = 0;

                        String msgTitle = labels.getString("SYNC_SUCCESS_TITLE");
                        String msgText = labels.getString("SYNC_SUCCESS_TEXT");

                        boolean error = false;

                        try {

                            PreparedStatement st = conn.prepareStatement(SQL_CLIENT_SYNC);
                            st.setString(1, syncCode);

                            ResultSet rs = st.executeQuery();

                            if (rs.next()) {
                                syncGroup1 = rs.getInt("sync_group_id");
                                uid1 = rs.getInt("uid");
                            } else {
                                msgTitle = labels.getString("SYNC_CODE_TITLE");
                                msgText = labels.getString("SYNC_CODE_TEXT");
                                errorList.addError("SYNC_CODE_TEXT");
                                getResponse().setStatus(Status.SUCCESS_OK);
                                error = true;
                                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                                // new Object[] {uuid}));
                            }

                            // close result set
                            SQLHelper.closeResultSet(rs);

                            // close prepared statement
                            SQLHelper.closePreparedStatement(st);

                            st = conn.prepareStatement(SQL_CLIENT);
                            st.setString(1, uuid.toString());

                            rs = st.executeQuery();

                            if (rs.next()) {
                                syncGroup2 = rs.getInt("sync_group_id");
                                uid2 = rs.getInt("uid");
                            } else {
                                msgTitle = labels.getString("SYNC_UUID_TITLE");
                                msgText = labels.getString("SYNC_UUID_TEXT");
                                errorList.addError("SYNC_UUID_TEXT");
                                getResponse().setStatus(Status.SUCCESS_OK);
                                error = true;
                                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                                // new Object[] {uuid}));
                            }

                            // close result set
                            SQLHelper.closeResultSet(rs);

                            // close prepared statement
                            SQLHelper.closePreparedStatement(st);

                            if (syncGroup1 > 0 && syncGroup1 == syncGroup2) {
                                msgTitle = labels.getString("SYNC_GROUP_TITLE");
                                msgText = labels.getString("SYNC_GROUP_TEXT");
                                errorList.addError("SYNC_GROUP_TEXT");
                                getResponse().setStatus(Status.SUCCESS_OK);
                                error = true;
                            }

                            if (uid1 > 0 && uid1 == uid2) {
                                msgTitle = labels.getString("SYNC_CLIENT_TITLE");
                                msgText = labels.getString("SYNC_CLIENT_TEXT");
                                errorList.addError("SYNC_CLIENT_TEXT");
                                getResponse().setStatus(Status.SUCCESS_OK);
                                error = true;
                            }

                            if (!error)
                                if (syncGroup1 == 0 && syncGroup2 == 0) {

                                    int key = 0;

                                    // create new group
                                    st = conn.prepareStatement(SQL_INSERT_SYNC_GROUP,
                                            Statement.RETURN_GENERATED_KEYS);

                                    logger.debug(st.toString());
                                    int affectedRows = st.executeUpdate();
                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_STORE_SYNC_GROUP");
                                    else {

                                        rs = st.getGeneratedKeys();
                                        if (rs.next())
                                            // Retrieve the auto generated
                                            // key(s).
                                            key = rs.getInt(1);
                                    }

                                    // close result set
                                    SQLHelper.closeResultSet(rs);

                                    // close prepared statement
                                    SQLHelper.closePreparedStatement(st);

                                    if (key > 0) {
                                        st = conn
                                                .prepareStatement(SQL_UPDATE_CLIENT);
                                        st.setInt(1, key);
                                        st.setInt(2, uid1);
                                        st.setInt(3, uid2);

                                        logger.debug(st.toString());
                                        affectedRows = st.executeUpdate();

                                        if (affectedRows == 0)
                                            errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    }

                                } else if (syncGroup1 == 0 && syncGroup2 > 0) {

                                    // add 1 to 2

                                    st = conn.prepareStatement(SQL_UPDATE_CLIENT_UID);
                                    st.setInt(1, syncGroup2);
                                    st.setInt(2, uid1);

                                    logger.debug(st.toString());
                                    final int affectedRows = st.executeUpdate();

                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");

                                } else if (syncGroup1 > 0 && syncGroup2 == 0) {

                                    // add 2 to 1

                                    st = conn.prepareStatement(SQL_UPDATE_CLIENT_UID);
                                    st.setInt(1, syncGroup1);
                                    st.setInt(2, uid2);

                                    logger.debug(st.toString());
                                    final int affectedRows = st.executeUpdate();

                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");

                                } else if (syncGroup1 > 0 && syncGroup2 > 0) {

                                    // add all of 2 to 1

                                    st = conn
                                            .prepareStatement(SQL_UPDATE_CLIENT_SYNC_GROUP_UID);
                                    st.setInt(1, syncGroup1);
                                    st.setInt(2, syncGroup2);

                                    logger.debug(st.toString());
                                    int affectedRows = st.executeUpdate();

                                    if (affectedRows == 0)
                                        errorList.addError("ERROR_DB_UPDATE_SYNC_GROUP");
                                    else {

                                        // Delete empty group
                                        st = conn.prepareStatement(SQL_DELETE_SYNC_GROUP);
                                        st.setInt(1, syncGroup2);

                                        logger.debug(st.toString());
                                        affectedRows = st.executeUpdate();

                                        if (affectedRows == 0)
                                            errorList.addError("ERROR_DB_DELETE_SYNC_GROUP");
                                    }

                                }

                            // close result set
                            SQLHelper.closeResultSet(rs);

                            // close prepared statement
                            SQLHelper.closePreparedStatement(st);

                        } catch (final SQLException e) {
                            logger.error(e.getMessage());
                            errorList.addError("ERROR_DB_GET_SYNC_SQL");
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                            getResponse().setStatus(Status.SUCCESS_OK);
                        }

                        if (errorList.getLength() == 0) {

                            final JSONObject jsonItem = new JSONObject();

                            jsonItem.put("msg_title", msgTitle);
                            jsonItem.put("msg_text", msgText);
                            jsonItem.put("success", !error);
                            syncList.put(jsonItem);

                        }
                    }

                    answer.put("sync", syncList);

                } else
                    errorList.addError("ERROR_DB_CONNECTION");

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
            getResponse().setStatus(Status.SUCCESS_OK);
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

    private boolean checkSyncCode(String syncCode, ErrorList errorList) {
        if (syncCode.contains(" ")) {
            getResponse().setStatus(Status.SUCCESS_OK);
            errorList.addError("SYNC_CODE_TITLE");
            return false;
        } else if (isInValid(syncCode)) {
            getResponse().setStatus(Status.SUCCESS_OK);
            errorList.addError("SYNC_CODE_TITLE");
            return false;
        } else if (syncCode.length() != 12) {
            getResponse().setStatus(Status.SUCCESS_OK);
            errorList.addError("SYNC_CODE_TITLE");
            return false;
        }

        return true;
    }

    private boolean isInValid(String syncCode) {
        Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
        Matcher hasSpecial = special.matcher(syncCode);
        return hasSpecial.find();
    }

}
