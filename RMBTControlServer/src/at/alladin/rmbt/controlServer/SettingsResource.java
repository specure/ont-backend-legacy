/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.shared.*;
import at.alladin.rmbt.shared.db.Client;
import at.alladin.rmbt.shared.db.QoSTestTypeDesc;
import at.alladin.rmbt.shared.db.dao.QoSTestTypeDescDao;
import at.alladin.rmbt.shared.db.model.AdvertisedSpeedOption;
import at.alladin.rmbt.shared.db.repository.AdvertisedSpeedOptionRepository;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

public class SettingsResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(SettingsResource.class);

    private static final String SQL_GROUP_NAME = "SELECT DISTINCT group_name" + " FROM test t"
            + " JOIN network_type nt ON t.network_type=nt.uid"
            + " WHERE t.deleted = false AND t.status = 'FINISHED' "
            + " AND (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? ))"
            + " AND group_name IS NOT NULL ORDER BY group_name";

    private static final String SQL_MODEL = "SELECT DISTINCT COALESCE(adm.fullname, t.model) model"
            + " FROM test t LEFT JOIN device_map adm ON adm.codename=t.model"
//            + " WHERE (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? )) AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED' ORDER BY model ASC";
            + " WHERE (t.client_id IN (SELECT ? UNION SELECT uid FROM client WHERE sync_group_id = ? )) AND t.deleted = false AND t.time IS NOT NULL AND t.status = 'FINISHED' ORDER BY model ASC";

    /**
     * @param entity
     * @return
     */
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
        logger.info("New settings request from " + clientIpRaw);

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

                // System.out.println(request.toString(4));

                if (conn != null) {

                    final Client client = new Client(conn);
                    int typeId = 0;

                    if (request.optString("type").length() > 0) {
                        typeId = client.getTypeId(request.getString("type"));
                        if (client.hasError())
                            errorList.addError(client.getError());
                    }

                    final List<String> clientNames = Arrays.asList(settings.getString("RMBT_CLIENT_NAME")
                            .split(",\\s*"));

                    final JSONArray settingsList = new JSONArray();
                    final JSONObject jsonItem = new JSONObject();

                    if (clientNames.contains(request.optString("name")) && typeId > 0) {

                        // String clientName = request.getString("name");
                        // String clientVersionCode =
                        // request.getString("version_name");
                        // String clientVersionName =
                        // request.optString("version_code", "");

                        UUID uuid = null;
                        long clientUid = 0;

                        final String uuidString = request.optString("uuid", "");
                        try {
                            if (!Strings.isNullOrEmpty(uuidString))
                                uuid = UUID.fromString(uuidString);
                        } catch (IllegalArgumentException e) // not a valid uuid
                        {
                            logger.error(e.getMessage());
                        }

                        if (uuid != null && errorList.getLength() == 0) {
                            clientUid = client.getClientByUuid(uuid);
                            if (client.hasError())
                                errorList.addError(client.getError());
                        }

                        boolean tcAccepted = request.optInt("terms_and_conditions_accepted_version", 0) > 0; // accept any version for now
                        if (!tcAccepted) // allow old non-version parameter
                            tcAccepted = request.optBoolean("terms_and_conditions_accepted", false);
                        {
                            if (tcAccepted && (uuid == null || clientUid == 0)) {

                                final Timestamp tstamp = java.sql.Timestamp.valueOf(new Timestamp(System
                                        .currentTimeMillis()).toString());

                                final Calendar timeWithZone = Helperfunctions.getTimeWithTimeZone(Helperfunctions
                                        .getTimezoneId());

                                client.setTimeZone(timeWithZone);
                                client.setTime(tstamp);
                                client.setClient_type_id(typeId);
                                client.setTcAccepted(tcAccepted);

                                uuid = client.storeClient();

                                if (client.hasError())
                                    errorList.addError(client.getError());
                                else
                                    jsonItem.put("uuid", uuid.toString());
                            }

                            if (client.getUid() > 0) {
                                /* map server */
                                final Series<Parameter> ctxParams = getContext().getParameters();
                                final String host = ctxParams.getFirstValue("RMBT_MAP_HOST");
                                final String sslStr = ctxParams.getFirstValue("RMBT_MAP_SSL");
                                final String portStr = ctxParams.getFirstValue("RMBT_MAP_PORT");
                                if (host != null && sslStr != null && portStr != null) {
                                    JSONObject mapServer = new JSONObject();
                                    mapServer.put("host", host);
                                    mapServer.put("port", Integer.parseInt(portStr));
                                    mapServer.put("ssl", Boolean.parseBoolean(sslStr));
                                    jsonItem.put("map_server", mapServer);
                                }

                                // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                // HISTORY / FILTER

                                final JSONObject subItem = new JSONObject();

                                final JSONArray netList = new JSONArray();

                                try {

                                    // deviceList:

                                    subItem.put("devices", getSyncGroupDeviceList(errorList, client));

                                    // network_type:

                                    final PreparedStatement st = conn.prepareStatement(SQL_GROUP_NAME);

                                    st.setLong(1, client.getUid());
                                    st.setInt(2, client.getSync_group_id());

                                    final ResultSet rs = st.executeQuery();

                                    if (rs != null)
                                        while (rs.next())
                                            // netList.put(Helperfunctions.getNetworkTypeName(rs.getInt("network_type")));
                                            //netList.put(rs.getString("group_name"));
                                            //SDRU-127ß
                                            //netList.put(Helperfunctions.getNetworkTypeGroupName(rs.getString("group_name"), lang, labels));
                                            // ONT-1429
                                            netList.put(rs.getString("group_name"));
                                    else
                                        errorList.addError("ERROR_DB_GET_SETTING_HISTORY_NETWORKS_SQL");

                                    // close result set
                                    SQLHelper.closeResultSet(rs);

                                    // close prepared statement
                                    SQLHelper.closePreparedStatement(st);

                                    subItem.put("networks", netList);

                                } catch (final SQLException e) {
                                    logger.error(e.getMessage());
                                    errorList.addError("ERROR_DB_GET_SETTING_HISTORY_SQL");
                                    // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
                                }

                                if (errorList.getLength() == 0)
                                    jsonItem.put("history", subItem);

                            } else
                                errorList.addError("ERROR_CLIENT_UUID");
                        }

                        final String hasAdvertisedSpeedOption = getSetting("has_advertised_speed_option");
                        if (hasAdvertisedSpeedOption != null && Boolean.parseBoolean(hasAdvertisedSpeedOption.trim())) {
                            AdvertisedSpeedOptionRepository advSpdOptnRepo = new AdvertisedSpeedOptionRepository(conn);
                            List<AdvertisedSpeedOption> advSpdOptnList = advSpdOptnRepo.getAllEnabled();
                            Gson gson = new Gson();
                            JSONArray jsonArray = new JSONArray(gson.toJson(advSpdOptnList));
                            jsonItem.put("advertised_speed_option", jsonArray);
                        }


                        //also put there: basis-urls for all services
                        final JSONObject jsonItemURLs = new JSONObject();
                        jsonItemURLs.put("open_data_prefix", getSetting("url_open_data_prefix", lang));
                        jsonItemURLs.put("statistics", getSetting("url_statistics", lang));
                        jsonItemURLs.put("control_ipv4_only", getSetting("control_ipv4_only", lang));
                        jsonItemURLs.put("control_ipv6_only", getSetting("control_ipv6_only", lang));
                        jsonItemURLs.put("url_ipv4_check", getSetting("url_ipv4_check", lang));
                        jsonItemURLs.put("url_ipv6_check", getSetting("url_ipv6_check", lang));

                        jsonItem.put("urls", jsonItemURLs);

                        //Survey settings
                        JSONObject jsonItemSurvey = new JSONObject();
                        InetAddress clientAddress = InetAddresses.forString(clientIpRaw);
                        String geoIpCountry = GeoIPHelper.getInstance().lookupCountry(clientAddress);
                        String survey_country_code = getSetting("survey_country_code");
                        Timestamp timeStamp = Timestamp.valueOf((getSetting("survey_start")));

                        if(survey_country_code.equals(geoIpCountry) || survey_country_code.equals("all")) {
                            jsonItemSurvey.put("is_active_service", Boolean.valueOf(getSetting("is_active_service")));
                            jsonItemSurvey.put("survey_url", getSetting("survey_url"));
                        }else {
                            jsonItemSurvey.put("is_active_service", false);
                            jsonItemSurvey.put("survey_url", "null");
                        }

                        jsonItemSurvey.put("date_started", timeStamp.getTime());

                        jsonItem.put("survey_settings", jsonItemSurvey);


                        final JSONObject jsonControlServerVersion = new JSONObject();
                        jsonControlServerVersion.put("control_server_version", RevisionHelper.getVerboseRevision());
                        jsonItem.put("versions", jsonControlServerVersion);
                        try {
                            final Locale locale = new Locale(lang);
                            final QoSTestTypeDescDao testTypeDao = new QoSTestTypeDescDao(conn, locale);
                            final JSONArray testTypeDescArray = new JSONArray();
                            for (QoSTestTypeDesc desc : testTypeDao.getAll()) {
                                JSONObject json = new JSONObject();
                                json.put("test_type", desc.getTestType().name());
                                json.put("name", desc.getName());
                                testTypeDescArray.put(json);
                            }
                            jsonItem.put("qostesttype_desc", testTypeDescArray);
                        } catch (SQLException e) {
                            errorList.addError("ERROR_DB_CONNECTION");
                            logger.error(e.getMessage());
                        }

                        settingsList.put(jsonItem);
                        answer.put("settings", settingsList);

                        //debug: print settings response (JSON)
                        //System.out.println(settingsList);

                    } else
                        errorList.addError("ERROR_CLIENT_VERSION");

                } else
                    errorList.addError("ERROR_DB_CONNECTION");

            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSDON Data " + e.toString());
            } catch (SQLException e) {
                errorList.addError("ERROR_DB_CONNECTION");
                logger.error(e.getMessage());
            }
        else
            errorList.addErrorString("Expected request is missing.");

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        answerString = answer.toString();

//        try
//        {
//            System.out.println(answer.toString(4));
//        }
//        catch (final JSONException e)
//        {
//            e.printStackTrace();
//        }


        answerString = answer.toString();
        long elapsedTime = System.currentTimeMillis() - startTime;
        logger.info("Settings request from "+ clientIpRaw + " completed " + Long.toString(elapsedTime) + " ms");

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }

    /**
     * @param entity
     * @return
     */
    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private JSONArray getSyncGroupDeviceList(final ErrorList errorList, final Client client) throws SQLException {

        JSONArray ownDeviceList = null;

        final PreparedStatement st = conn
                .prepareStatement(SQL_MODEL);

        st.setLong(1, client.getUid());
        st.setInt(2, client.getSync_group_id());

//        System.out.println(st.toString());

        final ResultSet rs = st.executeQuery();
        if (rs != null) {

            ownDeviceList = new JSONArray();

            while (rs.next()) {
                final String model = rs.getString("model");
                if (model == null || model.isEmpty())
                    ownDeviceList.put("Unknown Device");
                else
                    ownDeviceList.put(model);
            }
        } else
            errorList.addError("ERROR_DB_GET_SETTING_HISTORY_DEVICES_SQL");

        // close result set
        SQLHelper.closeResultSet(rs);

        // close prepared statement
        SQLHelper.closePreparedStatement(st);

        return ownDeviceList;
    }

}
