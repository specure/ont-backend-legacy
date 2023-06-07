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
package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.mapServer.MapServerOptions.MapFilter;
import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.mapServer.MapServerOptions.SQLFilter;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.specure.rmbt.shared.res.customer.CustomerResource;
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

public class MarkerResourceV2 extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(MarkerResourceV2.class);

    private static int MAX_PROVIDER_LENGTH = 22;
    private static int CLICK_RADIUS = 10;

    // show device info in bubble( AKOS)
    private final Boolean showDeviceInfoOnMap = CustomerResource.getInstance().showDeviceInfoOnMap();

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " + entity);

        //System.out.println(entity);
        addAllowOrigin();

        final Classification classification = Classification.getInstance();
        final MapServerOptions mso = MapServerOptions.getInstance(classification);


        JSONObject request = null;

        final JSONObject answer = new JSONObject();

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                String lang = request.optString("language");

                // Load Language Files for Client

                final List<String> langs =
                        Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang))
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

                // System.out.println(request.toString(4));

                final JSONObject coords = request.getJSONObject("coords");

                final int zoom;
                double geo_x = 0;
                double geo_y = 0;
                int size = 0;

                boolean useXY = false;
                boolean useLatLon = false;

                if (coords.has("x") && coords.has("y"))
                    useXY = true;
                else if (coords.has("lat") && coords.has("lon"))
                    useLatLon = true;

                if (coords.has("z") && (useXY || useLatLon)) {
                    zoom = coords.optInt("z");
                    if (useXY) {
                        geo_x = coords.optDouble("x");
                        geo_y = coords.optDouble("y");
                    } else if (useLatLon) {
                        final double tmpLat = coords.optDouble("lat");
                        final double tmpLon = coords.optDouble("lon");
                        geo_x = GeoCalc.lonToMeters(tmpLon);
                        geo_y = GeoCalc.latToMeters(tmpLat);
                        // System.out.println(String.format("using %f/%f", geo_x, geo_y));
                    }

                    if (coords.has("size"))
                        size = coords.getInt("size");

                    if (zoom != 0 && geo_x != 0 && geo_y != 0) {
                        double radius = 0;
                        if (size > 0)
                            radius = size * GeoCalc.getResFromZoom(256, zoom); // TODO use real tile size
                        else
                            radius = CLICK_RADIUS * GeoCalc.getResFromZoom(256, zoom); // TODO use real tile size
                        final double geo_x_min = geo_x - radius;
                        final double geo_x_max = geo_x + radius;
                        final double geo_y_min = geo_y - radius;
                        final double geo_y_max = geo_y + radius;

                        String hightlightUUIDString = null;
                        UUID highlightUUID = null;

                        final JSONObject mapOptionsObj = request.getJSONObject("options");
                        String optionStr = mapOptionsObj.optString("map_options");
                        if (optionStr == null || optionStr.length() == 0) // set
                            // default
                            optionStr = "mobile/download";

                        final MapOption mo = mso.getMapOptionMap().get(optionStr);

                        final List<SQLFilter> filters = new ArrayList<>(mso.getDefaultMapFilters());
                        filters.add(mso.getAccuracyMapFilter());

                        final JSONObject mapFilterObj = request.getJSONObject("filter");

                        final Iterator<?> keys = mapFilterObj.keys();

                        while (keys.hasNext()) {
                            final String key = (String) keys.next();
                            if (mapFilterObj.get(key) instanceof Object)
                                if (key.equals("highlight"))
                                    hightlightUUIDString = mapFilterObj.getString(key);
                                else {
                                    final MapFilter mapFilter = mso.getMapFilterMap().get(key);
                                    if (mapFilter != null) {
                                        try {
                                            filters.add(mapFilter.getFilter(String.valueOf(mapFilterObj.get(key))));
                                        } catch (Exception e) {
                                            filters.add(mapFilter.getFilter(mapFilterObj.getString(key)));
                                        }
                                    }
                                }
                        }

                        if (hightlightUUIDString != null)
                            try {
                                highlightUUID = UUID.fromString(hightlightUUIDString);
                            } catch (final Exception e) {
                                logger.error(e.getMessage());
                                highlightUUID = null;
                            }

                        if (conn != null) {
                            PreparedStatement ps = null;
                            ResultSet rs = null;

                            final StringBuilder whereSQL = new StringBuilder(mo.sqlFilter);
                            for (final SQLFilter sf : filters)
                                whereSQL.append(" AND ").append(sf.where);

                            final String sql = String.format("SELECT"
                                    + (useLatLon ? " geo_lat lat, geo_long lon"
                                    : " ST_X(t.location) x, ST_Y(t.location) y")
                                    + ", jpl.voip_result_jitter, jpl.voip_result_packet_loss"
                                    + ", t.uuid, t.time, t.timezone, t.speed_download, t.speed_upload, t.ping_median, t.network_type,"
                                    + " t.signal_strength, t.lte_rsrp, t.ss_rsrp, t.wifi_ssid, t.network_operator_name, t.network_operator,"
                                    + " t.network_sim_operator, t.network_sim_operator_name, t.roaming_type, t.public_ip_as_name, " // TODO:
                                    // sim_operator
                                    // obsoleted by
                                    // sim_name
                                    + " pMob.shortname mobile_provider_name," // TODO: obsoleted by
                                    // mobile_network_name
                                    + " prov.shortname provider_text, t.open_test_uuid,"
                                    + " COALESCE(mnwk.shortname,mnwk.name) mobile_network_name,"
                                    + " COALESCE(msim.shortname,msim.name) mobile_sim_name"
                                    + (highlightUUID == null ? "" : " , c.uid, c.uuid")
                                    + (showDeviceInfoOnMap ? ",t.model, t.plattform, t.os_version " : "")//only akos shows device infos
                                    + " FROM v_test t"
                                    + " LEFT JOIN test_jpl jpl ON t.uuid = jpl.test_uid"
                                    + " LEFT JOIN mccmnc2name mnwk ON t.mobile_network_id=mnwk.uid"
                                    + " LEFT JOIN mccmnc2name msim ON t.mobile_sim_id=msim.uid"
                                    + " LEFT JOIN provider prov" + " ON t.provider_id=prov.uid"
                                    + " LEFT JOIN provider pMob" + " ON t.mobile_provider_id=pMob.uid"
                                    + (highlightUUID == null ? ""
                                    : " LEFT JOIN client c ON (t.client_id=c.uid AND c.uuid=?)")
                                    + " WHERE" + " %s"
                                    + " AND location && ST_SetSRID(ST_MakeBox2D(ST_Point(?,?), ST_Point(?,?)), 900913)"
                                    + " ORDER BY" + (highlightUUID == null ? "" : " c.uid ASC,") + " t.uid DESC"
                                    + " LIMIT 5", whereSQL);

                            // System.out.println("SQL: " + sql);
                            ps = conn.prepareStatement(sql);

                            int i = 1;

                            if (highlightUUID != null)
                                ps.setObject(i++, highlightUUID);

                            for (final SQLFilter sf : filters)
                                i = sf.fillParams(i, ps);
                            ps.setDouble(i++, geo_x_min);
                            ps.setDouble(i++, geo_y_min);
                            ps.setDouble(i++, geo_x_max);
                            ps.setDouble(i++, geo_y_max);

                            logger.debug(ps.toString());
                            if (ps.execute()) {

                                final Locale locale = new Locale(lang);
                                final Format format = new SignificantFormat(2, locale);

                                final JSONArray resultList = new JSONArray();

                                rs = ps.getResultSet();

                                while (rs.next()) {
                                    final JSONObject jsonItem = new JSONObject();

                                    JSONArray measurementJsonItemList = new JSONArray();

                                    // RMBTClient Info
                                    if (highlightUUID != null && rs.getString("uuid") != null)
                                        jsonItem.put("highlight", true);

                                    final double res_x = rs.getDouble(1);
                                    final double res_y = rs.getDouble(2);
                                    final String openTestUUID = rs.getObject("open_test_uuid").toString();

                                    jsonItem.put("lat", res_x);
                                    jsonItem.put("lon", res_y);
                                    jsonItem.put("open_test_uuid", "O" + openTestUUID);
                                    // marker.put("uid", uid);

                                    final Date date = rs.getTimestamp("time");
                                    final String tzString = rs.getString("timezone");
                                    final DateFormat dateFormat =
                                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                                    if (!Strings.isNullOrEmpty(tzString))
                                        dateFormat.setTimeZone(TimeZone.getTimeZone(tzString));
                                    jsonItem.put("time_string", dateFormat.format(date));

                                    final int fieldDown = rs.getInt("speed_download");
                                    JSONObject singleItem = new JSONObject();
                                    singleItem.put("title", labels.getString("RESULT_DOWNLOAD"));
                                    final String downloadString = String.format("%s %s",
                                            format.format(fieldDown / 1000d), labels.getString("RESULT_DOWNLOAD_UNIT"));
                                    singleItem.put("value", downloadString);
                                    singleItem.put("classification",
                                            Classification.classify(classification.THRESHOLD_DOWNLOAD, fieldDown));
                                    // singleItem.put("help", "www.rtr.at");

                                    measurementJsonItemList.put(singleItem);

                                    final int fieldUp = rs.getInt("speed_upload");
                                    singleItem = new JSONObject();
                                    singleItem.put("title", labels.getString("RESULT_UPLOAD"));
                                    final String uploadString = String.format("%s %s", format.format(fieldUp / 1000d),
                                            labels.getString("RESULT_UPLOAD_UNIT"));
                                    singleItem.put("value", uploadString);
                                    singleItem.put("classification",
                                            Classification.classify(classification.THRESHOLD_UPLOAD, fieldUp));
                                    // singleItem.put("help", "www.rtr.at");

                                    measurementJsonItemList.put(singleItem);

                                    final long fieldPing = rs.getLong("ping_median");
                                    if (fieldPing != Long.MAX_VALUE) {
                                        final int pingValue = (int) Math.round(rs.getDouble("ping_median") / 1000000d);
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("RESULT_PING"));
                                        final String pingString = String.format("%s %s", format.format(pingValue),
                                                labels.getString("RESULT_PING_UNIT"));
                                        singleItem.put("value", pingString);
                                        singleItem.put("classification",
                                                Classification.classify(classification.THRESHOLD_PING, fieldPing));
                                        // singleItem.put("help", "www.rtr.at");
                                    } else {
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("RESULT_PING"));
                                        final String pingString = labels.getString("RESULT_NOT_AVAILABLE");
                                        singleItem.put("value", pingString);
                                        singleItem.put("classification",
                                                Classification.classify(classification.THRESHOLD_PING, fieldPing));
                                    }

                                    measurementJsonItemList.put(singleItem);


                                    final int networkType = rs.getInt("network_type");

                                    final String signalField = rs.getString("signal_strength");
                                    if (signalField != null && signalField.length() != 0) {
                                        final int signalValue = rs.getInt("signal_strength");
                                        final int[] threshold =
                                                networkType == 99 || networkType == 0 ? classification.THRESHOLD_SIGNAL_WIFI
                                                        : classification.THRESHOLD_SIGNAL_MOBILE;
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("RESULT_SIGNAL"));
                                        singleItem.put("value",
                                                signalValue + " " + labels.getString("RESULT_SIGNAL_UNIT"));
                                        singleItem.put("classification",
                                                Classification.classify(threshold, signalValue));
                                        measurementJsonItemList.put(singleItem);
                                    }

                                    final String lteRsrpField = rs.getString("lte_rsrp");
                                    if (lteRsrpField != null && lteRsrpField.length() != 0) {
                                        final int lteRsrpValue = rs.getInt("lte_rsrp");
                                        final int[] threshold = classification.THRESHOLD_SIGNAL_RSRP;
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("RESULT_LTE_RSRP"));
                                        singleItem.put("value", lteRsrpValue + " " + labels.getString("RESULT_LTE_RSRP_UNIT"));
                                        singleItem.put("classification", Classification.classify(threshold, lteRsrpValue));
                                        measurementJsonItemList.put(singleItem);
                                    }

                                    // ONT-1310 - 5G in Android legacy App - standalone
                                    final String ssRsrpField = rs.getString("ss_rsrp");
                                    if (ssRsrpField != null && ssRsrpField.length() != 0) {
                                        final int ssRsrpValue = rs.getInt("ss_rsrp");
                                        final int[] threshold = classification.THRESHOLD_SIGNAL_SSRSRP;
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("RESULT_SSRSRP"));
                                        singleItem.put("value", ssRsrpValue + " " + labels.getString("RESULT_SSRSRP_UNIT"));
                                        singleItem.put("classification", Classification.classify(threshold, ssRsrpValue));
                                        measurementJsonItemList.put(singleItem);
                                    }

                                    // jitter and packet loss
                                    JSONObject jitterSingleItem = null;
                                    if (rs.getString("voip_result_jitter") != null && rs.getString("voip_result_jitter").equals("-") != true) {
                                        jitterSingleItem = new JSONObject();
                                        jitterSingleItem.put("title", labels.getString("RESULT_JITTER"));

                                        try {
                                            double jitter = Double.parseDouble(rs.getString("voip_result_jitter"));
                                            jitterSingleItem.put("value", jitter + " ms");
                                            jitterSingleItem.put("classification", Classification.classify(classification.THRESHOLD_JITTER, jitter));
                                        } catch (Exception e) {
                                            logger.error(e.getMessage());
                                            jitterSingleItem = null;
                                        }

                                    }

                                    JSONObject packetLossSingleItem = null;
                                    if (rs.getString("voip_result_packet_loss") != null && rs.getString("voip_result_packet_loss").equals("100.0") != true) {
                                        packetLossSingleItem = new JSONObject();
                                        packetLossSingleItem.put("title", labels.getString("RESULT_PACKET_LOSS"));

                                        try {
                                            double packetLoss = Double.parseDouble(rs.getString("voip_result_packet_loss"));
                                            packetLossSingleItem.put("value", packetLoss + " %");
                                            packetLossSingleItem.put("classification", Classification.classify(classification.THRESHOLD_PACKET_LOSS, packetLoss));
                                        } catch (Exception e) {
                                            logger.error(e.getMessage());
                                            packetLossSingleItem = null;
                                        }

                                    }

                                    // check if jitter/packe loss are valid
                                    if (jitterSingleItem != null && packetLossSingleItem != null) {
                                        measurementJsonItemList.put(jitterSingleItem);
                                        measurementJsonItemList.put(packetLossSingleItem);
                                    }

                                    jsonItem.put("measurement", measurementJsonItemList);


                                    JSONArray jsonItemList = new JSONArray();

                                    singleItem = new JSONObject();
                                    singleItem.put("title", labels.getString("RESULT_NETWORK_TYPE"));
                                    // singleItem.put("value", Helperfunctions.getNetworkTypeName(networkType));
                                    singleItem.put("value",
                                            Helperfunctions.getNetworkTypeNameTranslated(networkType, lang, labels));
                                    jsonItemList.put(singleItem);


                                    if (networkType == 98 || networkType == 99) // mobile wifi or browser
                                    {
                                        String providerText = labels.getString("RESULT_UNKNOWN");
                                        if (rs.getString("provider_text") != null || rs.getString("public_ip_as_name") != null) {
                                            providerText = MoreObjects.firstNonNull(rs.getString("provider_text"),
                                                    rs.getString("public_ip_as_name"));
                                        }

                                        if (!Strings.isNullOrEmpty(providerText)) {
                                            if (providerText.length() > (MAX_PROVIDER_LENGTH + 3)) {
                                                providerText = providerText.substring(0, MAX_PROVIDER_LENGTH) + "...";
                                            }

                                            singleItem = new JSONObject();
                                            singleItem.put("title", labels.getString("RESULT_PROVIDER"));
                                            singleItem.put("value", providerText);
                                            jsonItemList.put(singleItem);
                                        }
                                        if (networkType == 99) // mobile wifi
                                        {
                                            if (highlightUUID != null && rs.getString("uuid") != null) // own test
                                            {
                                                final String ssid = rs.getString("wifi_ssid");
                                                if (ssid != null && ssid.length() != 0) {
                                                    singleItem = new JSONObject();
                                                    singleItem.put("title", labels.getString("RESULT_WIFI_SSID"));
                                                    singleItem.put("value", ssid.toString());
                                                    jsonItemList.put(singleItem);
                                                }
                                            }
                                        }
                                    } else // mobile
                                    {
                                        String networkOperator = rs.getString("network_operator");
                                        String networkOperatorName = rs.getString("network_operator_name");
                                        String mobileNetworkName = rs.getString("mobile_network_name");
                                        String simOperator = rs.getString("network_sim_operator");
                                        String simOperatorName = rs.getString("network_sim_operator_name");
                                        String mobileSimName = rs.getString("mobile_sim_name");
                                        final int roamingType = rs.getInt("roaming_type");
                                        // network
                                        if (!Strings.isNullOrEmpty(networkOperator)) {
                                            final String mobileNetworkString;
                                            if (roamingType != 2) {
                                                // not international roaming - display name of home network
                                                if (Strings.isNullOrEmpty(mobileSimName)) {
                                                    mobileNetworkString = networkOperatorName;
                                                } else {
                                                    mobileNetworkString =
                                                            String.format("%s (%s)", mobileSimName, networkOperator);
                                                }
                                            } else {
                                                // international roaming - display name of network
                                                if (Strings.isNullOrEmpty(mobileSimName)) {
                                                    mobileNetworkString = networkOperatorName;
                                                } else {
                                                    mobileNetworkString =
                                                            String.format("%s (%s)", mobileNetworkName, networkOperator);
                                                }
                                            }

                                            singleItem = new JSONObject();
                                            singleItem.put("title", labels.getString("RESULT_MOBILE_NETWORK"));
                                            singleItem.put("value", mobileNetworkString);
                                            jsonItemList.put(singleItem);
                                        }
                                        // home network (sim)
                                        else if (!Strings.isNullOrEmpty(simOperator)) {
                                            final String mobileNetworkString;

                                            if (Strings.isNullOrEmpty(mobileSimName)) {
                                                mobileNetworkString = simOperatorName;
                                            } else {
                                                mobileNetworkString = String.format("%s (%s)", mobileSimName, simOperator);
                                            }

                                            /*
                                             * if (!Strings.isNullOrEmpty(mobileProviderName)) { mobileNetworkString =
                                             * mobileProviderName; } else { mobileNetworkString = simOperator; }
                                             */

                                            singleItem = new JSONObject();
                                            singleItem.put("title", labels.getString("RESULT_HOME_NETWORK"));
                                            singleItem.put("value", mobileNetworkString);
                                            jsonItemList.put(singleItem);
                                        }

                                        if (roamingType > 0) {
                                            singleItem = new JSONObject();
                                            singleItem.put("title", labels.getString("RESULT_ROAMING"));
                                            singleItem.put("value", Helperfunctions.getRoamingType(labels, roamingType));
                                            jsonItemList.put(singleItem);
                                        }

                                    }

                                    jsonItem.put("net", jsonItemList);

                                    // provide device info( only for AKOS customer)
                                    if (showDeviceInfoOnMap) {

                                        jsonItemList = new JSONArray();

                                        String fieldModel = rs.getString("model");
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("key_model"));
                                        if (fieldModel != null && fieldModel.isEmpty() == false) {
                                            singleItem.put("value", fieldModel);
                                        } else {
                                            singleItem.put("value", labels.getString("RESULT_UNKNOWN"));
                                        }
                                        jsonItemList.put(singleItem);

                                        String fieldPlatform = rs.getString("plattform");
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("key_plattform"));
                                        if (fieldPlatform != null && fieldPlatform.isEmpty() == false) {
                                            singleItem.put("value", fieldPlatform);
                                        } else {
                                            singleItem.put("value", labels.getString("RESULT_UNKNOWN"));
                                        }
                                        jsonItemList.put(singleItem);

                                        String fieldOsVersion = rs.getString("os_version");
                                        singleItem = new JSONObject();
                                        singleItem.put("title", labels.getString("key_os_version"));
                                        if (fieldOsVersion != null && fieldOsVersion.isEmpty() == false) {
                                            singleItem.put("value", fieldOsVersion);
                                        } else {
                                            singleItem.put("value", labels.getString("RESULT_UNKNOWN"));
                                        }
                                        jsonItemList.put(singleItem);

                                        // add complete device info
                                        jsonItem.put("device", jsonItemList);
                                    }

                                    resultList.put(jsonItem);

                                    if (resultList.length() == 0)
                                        logger.warn("Error getting Results.");
                                    // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                                    // new Object[] {uuid}));

                                }

                                answer.put("measurements", resultList);
                            } else
                                logger.warn("Error executing SQL.");
                        } else
                            logger.warn("No Database Connection.");
                    }
                } else
                    logger.warn("Expected request is missing.");

            } catch (final JSONException e) {
                logger.error(e.getMessage());
            } catch (final SQLException e) {
                logger.error(e.getMessage());
            }
        else
            logger.debug("No Request.");

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();

    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }


}
