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

import at.alladin.rmbt.shared.*;
import at.alladin.rmbt.shared.db.Client;
import at.alladin.rmbt.shared.qos.QoSUtil;
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
import java.text.DateFormat;
import java.text.Format;
import java.util.*;

public class HistoryResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(HistoryResource.class);

//    private static final String SQL_TEST_WITHOUT_SYN =
//            "SELECT * FROM (SELECT DISTINCT ON (t.uuid) t.uuid,"
//                    + " jpl.*, time, timezone, speed_upload, speed_download, ping_median, network_type, wifi_ssid, network_operator, network_operator_name, public_ip_as_name, nt.group_name network_type_group_name,"
//                    + " COALESCE(adm.fullname, t.model) model FROM test t"
//                    + " LEFT JOIN test_jpl jpl ON t.uuid = jpl.test_uid"
//                    + " LEFT JOIN device_map adm ON adm.codename=t.model"
//                    + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
////                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
//                    + " WHERE t.deleted = false AND t.status = 'FINISHED' AND t.time IS NOT NULL AND t.timezone IS NOT NULL"
//                    + " AND client_id = ? %s %s ORDER BY t.uuid) AS x ORDER BY time DESC %s";
//
//    private static final String SQL_TEST_WITH_SYN =
//            "SELECT * FROM (SELECT DISTINCT ON (t.uuid) t.uuid,"
//                    + " jpl.*, time, timezone, speed_upload, speed_download, ping_median, network_type, wifi_ssid, network_operator, network_operator_name, public_ip_as_name, nt.group_name network_type_group_name,"
//                    + " COALESCE(adm.fullname, t.model) model" + " FROM test t"
//                    + " LEFT JOIN test_jpl jpl ON t.uuid = jpl.test_uid"
//                    + " LEFT JOIN device_map adm ON adm.codename=t.model"
//                    + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
////                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
//                    + " WHERE t.deleted = false AND t.status = 'FINISHED' AND t.time IS NOT NULL AND t.timezone IS NOT NULL"
//                    + " AND (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? ))"
//                    + " %s %s ORDER BY t.uuid) AS x ORDER BY time DESC %s";

    private static final String SQL_TEST_WITHOUT_SYN =
            "SELECT * FROM (SELECT DISTINCT ON (t.uuid) t.uuid, t.uid,"
                    + " jpl.voip_result_jitter, jpl.voip_result_packet_loss, time, timezone, speed_upload,"
                    + " speed_download, ping_median, network_type, wifi_ssid, network_operator, network_operator_name,"
                    + " public_ip_as_name, nt.group_name network_type_group_name,"
                    + " COALESCE(adm.fullname, t.model) model, pPro.shortname as operator"
                    + " FROM test t"
                    + " LEFT JOIN test_jpl jpl ON t.uuid = jpl.test_uid"
                    + " LEFT JOIN device_map adm ON adm.codename=t.model"
                    + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
                    + " LEFT JOIN provider pPro ON t.provider_id=pPro.uid"
//                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
                    + " WHERE t.zero_measurement = false AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED' AND t.time IS NOT NULL AND t.timezone IS NOT NULL"
                    + " AND client_id = ? %s %s ORDER BY t.uuid) AS x ORDER BY time DESC %s";

    private static final String SQL_TEST_WITH_SYN =
            "SELECT * FROM (SELECT DISTINCT ON (t.uuid) t.uuid, t.uid,"
                    + " jpl.voip_result_jitter, jpl.voip_result_packet_loss, time, timezone, speed_upload,"
                    + " speed_download, ping_median, network_type, wifi_ssid, network_operator, network_operator_name,"
                    + " public_ip_as_name, nt.group_name network_type_group_name,"
                    + " COALESCE(adm.fullname, t.model) model, pPro.shortname as operator"
                    + " FROM test t"
                    + " LEFT JOIN test_jpl jpl ON t.uuid = jpl.test_uid"
                    + " LEFT JOIN device_map adm ON adm.codename=t.model"
                    + " LEFT JOIN network_type nt ON t.network_type=nt.uid"
                    + " LEFT JOIN provider pPro ON t.provider_id=pPro.uid"
//                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
                    + " WHERE t.zero_measurement = false AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED' AND t.time IS NOT NULL AND t.timezone IS NOT NULL"
                    + " AND (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? ))"
                    + " %s %s ORDER BY t.uuid) AS x ORDER BY time DESC %s";

    private static final String SQL_MNC = "select n.name as mnc_name from mccmnc2name n where n.mccmnc=?";

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        long startTime = System.currentTimeMillis();
        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        final String clientIpRaw = getIP();
        logger.info("New history request from " + clientIpRaw);
//    System.out.println(MessageFormat.format(labels.getString("NEW_HISTORY"), clientIpRaw));

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                String lang = request.optString("language");

                // Load Language Files for Client

                final List<String> langs =
                        Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                } else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

                // System.out.println(request.toString(4));

                if (conn != null) {
                    final Client client = new Client(conn);


                    if (request.optString("uuid").length() > 0
                            && client.getClientByUuid(UUID.fromString(request.getString("uuid"))) > 0) {

                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(4, locale);

                        String limitRequest = "";
                        if (request.optInt("result_limit", 0) != 0) {
                            final int limit = request.getInt("result_limit");

                            // get offset string if there is one
                            String offsetString = "";
                            if ((request.optInt("result_offset", 0) != 0)
                                    && (request.getInt("result_offset") >= 0)) {
                                offsetString = " OFFSET " + request.getInt("result_offset");
                            }

                            limitRequest = " LIMIT " + limit + offsetString;
                        }

                        final ArrayList<String> deviceValues = new ArrayList<>();
                        String deviceRequest = "";
                        if (request.optJSONArray("devices") != null) {
                            final JSONArray devices = request.getJSONArray("devices");

                            boolean checkUnknown = false;
                            final StringBuffer sb = new StringBuffer();
                            for (int i = 0; i < devices.length(); i++) {
                                final String device = devices.getString(i);

                                if (device.equals("Unknown Device"))
                                    checkUnknown = true;
                                else {
                                    if (sb.length() > 0)
                                        sb.append(',');
                                    deviceValues.add(device);
                                    sb.append('?');
                                }
                            }

                            if (sb.length() > 0)
                                deviceRequest = " AND (COALESCE(adm.fullname, t.model) IN (" + sb.toString() + ")"
                                        + (checkUnknown ? " OR model IS NULL OR model = ''" : "") + ")";
                            // System.out.println(deviceRequest);

                        }

                        final ArrayList<String> filterValues = new ArrayList<>();
                        String networksRequest = "";

                        if (request.optJSONArray("networks") != null) {
                            final JSONArray tmpArray = request.getJSONArray("networks");
                            final StringBuilder tmpString = new StringBuilder();

                            if (tmpArray.length() >= 1) {
                                tmpString.append("AND nt.group_name IN (");
                                boolean first = true;
                                for (int i = 0; i < tmpArray.length(); i++) {
                                    if (first)
                                        first = false;
                                    else
                                        tmpString.append(',');
                                    tmpString.append('?');
                                    filterValues.add(tmpArray.getString(i));
                                }
                                tmpString.append(')');
                            }
                            networksRequest = tmpString.toString();
                        }

                        final JSONArray historyList = new JSONArray();

                        final PreparedStatement st;

                        try {
                            if (client.getSync_group_id() == 0) {
                                // use faster request ignoring sync-group as user is not synced (id=0)
                                st = conn.prepareStatement(String.format(SQL_TEST_WITHOUT_SYN, deviceRequest,
                                        networksRequest, limitRequest));
                            } else { // use slower request including sync-group if client is synced
                                st = conn.prepareStatement(
                                        String.format(SQL_TEST_WITH_SYN, deviceRequest, networksRequest, limitRequest));
                            }

                            int i = 1;
                            st.setLong(i++, client.getUid());
                            if (client.getSync_group_id() != 0)
                                st.setInt(i++, client.getSync_group_id());

                            for (final String value : deviceValues)
                                st.setString(i++, value);

                            for (final String filterValue : filterValues)
                                st.setString(i++, filterValue);

                            logger.debug(st.toString());
//              System.out.println(st.toString());

                            final ResultSet rs = st.executeQuery();

                            while (rs.next()) {
                                final JSONObject jsonItem = new JSONObject();

                                String testUuid = rs.getString("uuid");
                                jsonItem.put("test_uuid", testUuid);

                                final Date date = rs.getTimestamp("time");
                                final long time = date.getTime();
                                final String tzString = rs.getString("timezone");
                                final TimeZone tz = TimeZone.getTimeZone(tzString);
                                jsonItem.put("time", time);
                                jsonItem.put("timezone", tzString);
                                final DateFormat dateFormat =
                                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                                dateFormat.setTimeZone(tz);
                                jsonItem.put("time_string", dateFormat.format(date));

                                jsonItem.put("speed_upload", format.format(rs.getInt("speed_upload") / 1000d));
                                jsonItem.put("speed_download", format.format(rs.getInt("speed_download") / 1000d));

                                final long ping = rs.getLong("ping_median");
                                jsonItem.put("ping", format.format(ping / 1000000d));
                                // backwards compatibility for old clients
                                jsonItem.put("ping_shortest", format.format(ping / 1000000d));
                                jsonItem.put("model", rs.getString("model"));
                                jsonItem.put("network_type", Helperfunctions
                                        .getNetworkTypeGroupName(rs.getString("network_type_group_name"), lang, labels));

                                // for appscape-iPhone-Version: also add classification to the response
                                jsonItem.put("speed_upload_classification", Classification
                                        .classify(classification.THRESHOLD_UPLOAD, rs.getInt("speed_upload")));
                                jsonItem.put("speed_download_classification", Classification
                                        .classify(classification.THRESHOLD_DOWNLOAD, rs.getInt("speed_download")));
                                jsonItem.put("ping_classification", Classification
                                        .classify(classification.THRESHOLD_PING, rs.getLong("ping_median")));
                                // backwards compatibility for old clients
                                jsonItem.put("ping_shortest_classification", Classification
                                        .classify(classification.THRESHOLD_PING, rs.getLong("ping_median")));

                                // network_name
                                String wifi_ssid = rs.getString("wifi_ssid");
                                String network_operator_code = rs.getString("network_operator");
                                String network_operator_name = null;
                                // translate mnc code to operators name
                                if (network_operator_code != null && network_operator_code.isEmpty() == false) {
                                    network_operator_name = getMncName(network_operator_code);
                                }
                                // check if got operators name
                                if (network_operator_name == null || network_operator_name.isEmpty()) {
                                    network_operator_name = rs.getString("network_operator_name");
                                }
                                String public_ip_as_name = rs.getString("public_ip_as_name");
                                if (wifi_ssid != null && wifi_ssid.isEmpty() == false) {
                                    // wifi
                                    jsonItem.put("network_name", wifi_ssid);
                                } else if (network_operator_name != null && network_operator_name.isEmpty() == false) {
                                    // mobile
                                    jsonItem.put("network_name", network_operator_name);
                                } else if (public_ip_as_name != null && public_ip_as_name.isEmpty() == false) {
                                    // lan?
                                    jsonItem.put("network_name", public_ip_as_name);
                                } else {
                                    // unknown
                                    jsonItem.put("network_name", labels.getString("RESULT_UNKNOWN"));
                                }
                                // end

                                // operator
                                String operator = rs.getString("operator");

                                jsonItem.put("operator", (operator != null && !operator.isEmpty())? operator : labels.getString("RESULT_UNKNOWN"));

                                // qos_result
                                try {
                                    // ONT-523 - Long history loading AKOS production
                                    //String qos_result = QoSUtil.calculateTotalResultInPercentage(conn, new QoSUtil.TestUuid(testUuid, QoSUtil.TestUuid.UuidType.TEST_UUID));
                                    String qos_result = QoSUtil.calculateTotalResultInPercentage(conn, rs.getLong("uid"));
                                    jsonItem.put("qos_result", qos_result);
                                } catch (SQLException e) {
                                    logger.error(e.getMessage());
                                    jsonItem.put("qos_result", "-");
                                } catch (UnsupportedOperationException e) {
                                    logger.error(e.getMessage());
                                    jsonItem.put("qos_result", "-");
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                    jsonItem.put("qos_result", "-");
                                }

                                try {
                                    // check if exists jpl record
                                    if (rs.getString("voip_result_jitter") != null && rs.getString("voip_result_packet_loss") != null) {
                                        JSONObject resultJPL = new JSONObject();
                                        resultJPL.put("voip_result_jitter", rs.getString("voip_result_jitter"));
                                        resultJPL.put("voip_result_packet_loss", rs.getString("voip_result_packet_loss"));

                                        // check if it is number or no.
                                        try {
                                            double jitter = Double.parseDouble(rs.getString("voip_result_jitter"));
                                            resultJPL.put("classification_jitter", Classification.classify(classification.THRESHOLD_JITTER, jitter));
                                        } catch (Exception e) {
                                            resultJPL.put("classification_jitter", -1);
                                        }

                                        try {
                                            double packetLoss = Double.parseDouble(rs.getString("voip_result_packet_loss"));
                                            resultJPL.put("classification_packet_loss", Classification.classify(classification.THRESHOLD_PACKET_LOSS, packetLoss));
                                        } catch (Exception e) {
                                            resultJPL.put("classification_packet_loss", -1);
                                        }

                                        jsonItem.put("jpl", resultJPL);

                                    }
                                } catch (SQLException e) {
                                    logger.error(e.getMessage());
                                    jsonItem.put("qos_result", "-");
                                } catch (UnsupportedOperationException e) {
                                    logger.error(e.getMessage());
                                    jsonItem.put("qos_result", "-");
                                }

                                historyList.put(jsonItem);
                            }

                            if (historyList.length() == 0)
                                errorList.addError("ERROR_DB_GET_HISTORY");
                            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                            // new Object[] {uuid}));

                            // close result set
                            SQLHelper.closeResultSet(rs);

                            // close prepared statement
                            SQLHelper.closePreparedStatement(st);

                        } catch (final SQLException e) {
                            logger.error(e.getMessage());
                            errorList.addError("ERROR_DB_GET_HISTORY_SQL");
                            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                        }

                        answer.put("history", historyList);
                    } else
                        errorList.addError("ERROR_REQUEST_NO_UUID");

                } else
                    errorList.addError("ERROR_DB_CONNECTION");

            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
//        System.out.println("Error parsing JSDON Data " + e.toString());
                logger.error(e.toString());
            } catch (final IllegalArgumentException e) {
//                errorList.addError("ERROR_REQUEST_NO_UUID");
                errorList.addError("Invalid UUID");
                logger.error(e.toString());
            }
        else
            errorList.addErrorString("Expected request is missing.");

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
//      System.out.println("Error saving ErrorList: " + e.toString());
            logger.error("Error saving ErrorList: " + e.toString());
        }

        answerString = answer.toString();

        long elapsedTime = System.currentTimeMillis() - startTime;
//    System.out.println(MessageFormat.format(labels.getString("NEW_HISTORY_SUCCESS"), clientIpRaw,
//        Long.toString(elapsedTime)));
        logger.debug("History request from " + clientIpRaw + " completed " + Long.toString(elapsedTime) + "ms");

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

    private String getMncName(final String code) {
        String result = "";
        try {
            if (conn != null) {
                if (code != null && !code.isEmpty()) {
                    final PreparedStatement psMnc = conn.prepareStatement(SQL_MNC);
                    psMnc.setString(1, code);
                    logger.debug(psMnc.toString());
                    if (psMnc.execute()) {
                        final ResultSet rs = psMnc.getResultSet();
                        if (rs.next()) {
                            result = rs.getString("mnc_name");
                        }
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Error getMncName " + e.toString());
        }
        return result;
    }

}
