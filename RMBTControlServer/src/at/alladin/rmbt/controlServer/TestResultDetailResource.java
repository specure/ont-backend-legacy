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
import at.alladin.rmbt.shared.db.CellInfo;
import at.alladin.rmbt.shared.db.Client;
import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.TestNdt;
import at.alladin.rmbt.shared.db.fields.Field;
import at.alladin.rmbt.shared.db.fields.JsonField;
import at.alladin.rmbt.shared.db.fields.TimestampField;
import com.google.common.base.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.*;

public class TestResultDetailResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestResultDetailResource.class);

    protected boolean optionWithKeys = false;

    protected final String OPTION_WITH_KEYS = "WITH_KEYS";

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
        logger.info("New test result detail request from " + clientIpRaw);

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);
                System.out.println(request);

                String lang = request.optString("language");
                JSONArray options = request.optJSONArray("options");
                if (options != null) {
                    for (int i = 0; i < options.length(); i++) {
                        final String op = options.optString(i, null);
                        if (op != null) {
                            if (OPTION_WITH_KEYS.equals(op.toUpperCase(Locale.US))) {
                                jsonUtil.setOptionWithKeys(true);
                            }
                        }
                    }
                }

                // Load Language Files for Client

                final List<String> langs =
                        Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                    jsonUtil.setLabels(labels);
                } else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

                // System.out.println(request.toString(4));

                if (conn != null) {

                    final Client client = new Client(conn);
                    final Test test = new Test(conn);
                    TestNdt ndt = new TestNdt(conn);

                    final String testUuid = request.optString("test_uuid");
                    if (testUuid != null && test.getTestByUuid(UUID.fromString(testUuid)) > 0
                            && client.getClientByUid(test.getField("client_id").intValue())) {

                        if (!ndt.loadByTestId(test.getUid()))
                            ndt = null;

                        final Locale locale = new Locale(lang);
                        final Format format = new SignificantFormat(2, locale);

                        final JSONArray resultList = new JSONArray();

                        final JSONObject singleItem = jsonUtil.addObject(resultList, "time");
                        final Field timeField = test.getField("time");
                        if (!timeField.isNull()) {
                            final Date date = ((TimestampField) timeField).getDate();
                            final long time = date.getTime();
                            singleItem.put("time", time); // csv 3

                            final Field timezoneField = test.getField("timezone");
                            if (!timezoneField.isNull()) {
                                final String tzString = timezoneField.toString();
                                final TimeZone tz = TimeZone.getTimeZone(timezoneField.toString());
                                singleItem.put("timezone", tzString);


                                final DateFormat dateFormat =
                                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
                                dateFormat.setTimeZone(tz);
                                singleItem.put("value", dateFormat.format(date));

                                final Format tzFormat =
                                        new DecimalFormat("+0.##;-0.##", new DecimalFormatSymbols(locale));

                                final float offset = tz.getOffset(time) / 1000f / 60f / 60f;
                                jsonUtil.addString(resultList, "timezone",
                                        String.format("UTC%sh", tzFormat.format(offset)));
                            }
                        }

                        // speed download in Mbit/s (converted from kbit/s) - csv 10 (in kbit/s)
                        final Field downloadField = test.getField("speed_download");
                        if (!downloadField.isNull()) {
                            final String download = format.format(downloadField.doubleValue() / 1000d);
                            jsonUtil.addString(resultList, "speed_download",
                                    String.format("%s %s", download, labels.getString("RESULT_DOWNLOAD_UNIT")));
                        }

                        // speed upload im MBit/s (converted from kbit/s) - csv 11 (in kbit/s)
                        final Field uploadField = test.getField("speed_upload");
                        if (!uploadField.isNull()) {
                            final String upload = format.format(uploadField.doubleValue() / 1000d);
                            jsonUtil.addString(resultList, "speed_upload",
                                    String.format("%s %s", upload, labels.getString("RESULT_UPLOAD_UNIT")));
                        }

                        // median ping in ms
                        final Field pingMedianField = test.getField("ping_median");
                        if (!pingMedianField.isNull()) {
                            final String pingMedian = format.format(pingMedianField.doubleValue() / 1000000d);
                            jsonUtil.addString(resultList, "ping_median",
                                    String.format("%s %s", pingMedian, labels.getString("RESULT_PING_UNIT")));
                        }

                        // removed from result -> ONT-42 - BE: stop displaying ping variance in the detail results and add missing units to values
//                        // variance ping in ms
//                        final Field pingVarianceField = test.getField("ping_variance");
//                        if (!pingVarianceField.isNull()) {
//                            final String pingVariance =
//                                    format.format(pingVarianceField.doubleValue() / 1000000000000d); // E^12 because
//                            // variance is ^2
//                            // and stored as
//                            // ns
//                            jsonUtil.addString(resultList, "ping_variance",
//                                    String.format("%s %s", pingVariance, labels.getString("RESULT_PING_UNIT")));
//                        }

                        // signal strength RSSI in dBm - csv 13
                        final Field signalStrengthField = test.getField("signal_strength");
                        if (!signalStrengthField.isNull())
                            jsonUtil.addString(resultList, "signal_strength", String.format("%d %s",
                                    signalStrengthField.intValue(), labels.getString("RESULT_SIGNAL_UNIT")));

                        // signal strength RSRP in dBm (LTE) - csv 29
                        final Field lteRsrpField = test.getField("lte_rsrp");
                        if (!lteRsrpField.isNull())
                            jsonUtil.addString(resultList, "signal_rsrp", String.format("%d %s",
                                    lteRsrpField.intValue(), labels.getString("RESULT_SIGNAL_UNIT")));

                        // signal quality in LTE, RSRQ in dB
                        final Field lteRsrqField = test.getField("lte_rsrq");
                        if (!lteRsrqField.isNull())
                            jsonUtil.addString(resultList, "signal_rsrq", String.format("%d %s",
                                    lteRsrqField.intValue(), labels.getString("RESULT_DB_UNIT")));

                        // network, eg. "3G (HSPA+)
                        // TODO fix helper-function
                        final Field networkTypeField = test.getField("network_type");
                        if (!networkTypeField.isNull())
                            jsonUtil.addString(resultList, "network_type",
                                    // Helperfunctions.getNetworkTypeName(networkTypeField.intValue()));
                                    Helperfunctions.getNetworkTypeNameTranslated(networkTypeField.intValue(), lang, labels));

                        // geo-location
                        JSONObject locationJson = getGeoLocation(this, test, settings, conn, labels);

                        if (locationJson != null) {
                            if (locationJson.has("location")) {
                                jsonUtil.addString(resultList, "location", locationJson.getString("location"));
                            }
                            if (locationJson.has("country_location")) {
                                jsonUtil.addString(resultList, "country_location",
                                        locationJson.getString("country_location"));
                            }
                            if (locationJson.has("motion")) {
                                jsonUtil.addString(resultList, "motion", locationJson.getString("motion"));
                            }
                        }

                        // country derived from AS registry
                        final Field countryAsnField = test.getField("country_asn");
                        if (!countryAsnField.isNull())
                            jsonUtil.addString(resultList, "country_asn", countryAsnField.toString());

                        // country derived from geo-IP database
                        final Field countryGeoipField = test.getField("country_geoip");
                        if (!countryGeoipField.isNull())
                            jsonUtil.addString(resultList, "country_geoip", countryGeoipField.toString());

                        final Field zipCodeField = test.getField("zip_code");
                        if (!zipCodeField.isNull()) {
                            final String zipCode = zipCodeField.toString();
                            final int zipCodeInt = zipCodeField.intValue();
                            if (zipCodeInt > 999 || zipCodeInt <= 9999) // plausibility of zip code (must be 4
                                // digits in Austria)
                                jsonUtil.addString(resultList, "zip_code", zipCode);
                        }

                        final Field dataField = test.getField("data");
                        if (!Strings.isNullOrEmpty(dataField.toString())) {
                            final JSONObject data = new JSONObject(dataField.toString());

                            if (data.has("region"))
                                jsonUtil.addString(resultList, "region", data.getString("region"));
                            if (data.has("municipality"))
                                jsonUtil.addString(resultList, "municipality", data.getString("municipality"));
                            if (data.has("settlement"))
                                jsonUtil.addString(resultList, "settlement", data.getString("settlement"));
                            if (data.has("whitespace"))
                                jsonUtil.addString(resultList, "whitespace", data.getString("whitespace"));

                            if (data.has("cell_id"))
                                jsonUtil.addString(resultList, "cell_id", String.valueOf(data.getInt("cell_id")));
                            if (data.has("cell_name"))
                                jsonUtil.addString(resultList, "cell_name", data.getString("cell_name"));
                            if (data.has("cell_id_multiple") && data.getBoolean("cell_id_multiple"))
                                jsonUtil.addString(resultList, "cell_id_multiple",
                                        jsonUtil.getTranslation("value", "cell_id_multiple"));
                        }

                        final Field speedTestDurationField = test.getField("speed_test_duration");
                        if (!speedTestDurationField.isNull()) {
                            final String speedTestDuration =
                                    format.format(speedTestDurationField.doubleValue() / 1000d);
                            jsonUtil.addString(resultList, "speed_test_duration", String.format("%s %s",
                                    speedTestDuration, labels.getString("RESULT_DURATION_UNIT")));
                        }

                        // public client ip (private)
                        jsonUtil.addString(resultList, "client_public_ip", test.getField("client_public_ip"));

                        // AS number - csv 24
                        jsonUtil.addString(resultList, "client_public_ip_as", test.getField("public_ip_asn"));

                        // name of AS
                        jsonUtil.addString(resultList, "client_public_ip_as_name",
                                test.getField("public_ip_as_name"));

                        // reverse hostname (from ip) - (private)
                        jsonUtil.addString(resultList, "client_public_ip_rdns",
                                test.getField("public_ip_rdns"));

                        // operator - derived from provider_id (only for pre-defined operators)
                        // TODO replace provider-information by more generic information
                        jsonUtil.addString(resultList, "provider", test.getField("provider_id_name"));

                        // type of client local ip (private)
                        jsonUtil.addString(resultList, "client_local_ip",
                                test.getField("client_ip_local_type"));

                        // nat-translation of client - csv 23
                        jsonUtil.addString(resultList, "nat_type", test.getField("nat_type"));

                        // wifi base station id SSID (numberic) eg 01:2c:3d..
                        jsonUtil.addString(resultList, "wifi_ssid", test.getField("wifi_ssid"));
                        // wifi base station id - BSSID (text) eg 'my hotspot'
                        jsonUtil.addString(resultList, "wifi_bssid", test.getField("wifi_bssid"));

                        // nominal link speed of wifi connection in MBit/s
                        final Field linkSpeedField = test.getField("wifi_link_speed");
                        if (!linkSpeedField.isNull())
                            jsonUtil.addString(resultList, "wifi_link_speed", String.format("%s %s",
                                    linkSpeedField.toString(), labels.getString("RESULT_WIFI_LINK_SPEED_UNIT")));
                        // name of mobile network operator (eg. 'T-Mobile AT')
                        jsonUtil.addString(resultList, "network_operator_name",
                                test.getField("network_operator_name"));

                        // mobile network name derived from MCC/MNC of network, eg. '232-01'
                        final Field networkOperatorField = test.getField("network_operator");

                        // mobile provider name, eg. 'Hutchison Drei' (derived from mobile_provider_id)
                        // FIX SDNT-187
                        // final Field mobileProviderNameField = test.getField("mobile_provider_name");
                        final Field mobileProviderNameField = test.getField("network_operator_name");
                        if (mobileProviderNameField.isNull()) // eg. '248-02'
                            jsonUtil.addString(resultList, "network_operator", networkOperatorField);
                        else {
                            if (networkOperatorField.isNull())
                                jsonUtil.addString(resultList, "network_operator", mobileProviderNameField);
                            else // eg. 'Hutchison Drei (232-10)'
                                jsonUtil.addString(resultList, "network_operator",
                                        String.format("%s (%s)", mobileProviderNameField, networkOperatorField));
                        }

                        jsonUtil.addString(resultList, "network_sim_operator_name",
                                test.getField("network_sim_operator_name"));

                        final Field networkSimOperatorField = test.getField("network_sim_operator");
                        final Field networkSimOperatorTextField =
                                test.getField("network_sim_operator_mcc_mnc_text");
                        if (networkSimOperatorTextField.isNull())
                            jsonUtil.addString(resultList, "network_sim_operator", networkSimOperatorField);
                        else
                            jsonUtil.addString(resultList, "network_sim_operator",
                                    String.format("%s (%s)", networkSimOperatorTextField, networkSimOperatorField));

                        final Field roamingTypeField = test.getField("roaming_type");
                        if (!roamingTypeField.isNull())
                            jsonUtil.addString(resultList, "roaming",
                                    Helperfunctions.getRoamingType(labels, roamingTypeField.intValue()));

                        final long totalDownload = test.getField("total_bytes_download").longValue();
                        final long totalUpload = test.getField("total_bytes_upload").longValue();
                        final long totalBytes = totalDownload + totalUpload;
                        if (totalBytes > 0) {
                            final String totalBytesString = format.format(totalBytes / (1000d * 1000d));
                            jsonUtil.addString(resultList, "total_bytes", String.format("%s %s", totalBytesString,
                                    labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }

                        // interface volumes - total including control-server and pre-tests (and other tests)
                        final long totalIfDownload = test.getField("test_if_bytes_download").longValue();
                        final long totalIfUpload = test.getField("test_if_bytes_upload").longValue();
                        // output only total of down- and upload
                        final long totalIfBytes = totalIfDownload + totalIfUpload;
                        if (totalIfBytes > 0) {
                            final String totalIfBytesString = format.format(totalIfBytes / (1000d * 1000d));
                            jsonUtil.addString(resultList, "total_if_bytes", String.format("%s %s",
                                    totalIfBytesString, labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // interface volumes during test
                        // download test - volume in download direction
                        final long testDlIfBytesDownload =
                                test.getField("testdl_if_bytes_download").longValue();
                        if (testDlIfBytesDownload > 0l) {
                            final String testDlIfBytesDownloadString =
                                    format.format(testDlIfBytesDownload / (1000d * 1000d));
                            jsonUtil.addString(resultList, "testdl_if_bytes_download", String.format("%s %s",
                                    testDlIfBytesDownloadString, labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // download test - volume in upload direction
                        final long testDlIfBytesUpload = test.getField("testdl_if_bytes_upload").longValue();
                        if (testDlIfBytesUpload > 0l) {
                            final String testDlIfBytesUploadString =
                                    format.format(testDlIfBytesUpload / (1000d * 1000d));
                            jsonUtil.addString(resultList, "testdl_if_bytes_upload", String.format("%s %s",
                                    testDlIfBytesUploadString, labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // upload test - volume in upload direction
                        final long testUlIfBytesUpload = test.getField("testul_if_bytes_upload").longValue();
                        if (testUlIfBytesUpload > 0l) {
                            final String testUlIfBytesUploadString =
                                    format.format(testUlIfBytesUpload / (1000d * 1000d));
                            jsonUtil.addString(resultList, "testul_if_bytes_upload", String.format("%s %s",
                                    testUlIfBytesUploadString, labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }
                        // upload test - volume in download direction
                        final long testUlIfBytesDownload =
                                test.getField("testul_if_bytes_download").longValue();
                        if (testDlIfBytesDownload > 0l) {
                            final String testUlIfBytesDownloadString =
                                    format.format(testUlIfBytesDownload / (1000d * 1000d));
                            jsonUtil.addString(resultList, "testul_if_bytes_download", String.format("%s %s",
                                    testUlIfBytesDownloadString, labels.getString("RESULT_TOTAL_BYTES_UNIT")));
                        }

                        // start time download-test
                        final Field time_dl_ns = test.getField("time_dl_ns");
                        if (!time_dl_ns.isNull()) {
                            jsonUtil.addString(resultList, "time_dl",
                                    String.format("%s %s", format.format(time_dl_ns.doubleValue() / 1000000000d), // convert
                                            // ns
                                            // to
                                            // s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }

                        // duration download-test
                        final Field duration_download_ns = test.getField("nsec_download");
                        if (!duration_download_ns.isNull()) {
                            jsonUtil.addString(resultList, "duration_dl",
                                    String.format("%s %s",
                                            format.format(duration_download_ns.doubleValue() / 1000000000d), // convert ns
                                            // to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }

                        // start time upload-test
                        final Field time_ul_ns = test.getField("time_ul_ns");
                        if (!time_ul_ns.isNull()) {
                            jsonUtil.addString(resultList, "time_ul",
                                    String.format("%s %s", format.format(time_ul_ns.doubleValue() / 1000000000d), // convert
                                            // ns
                                            // to
                                            // s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }

                        // duration upload-test
                        final Field duration_upload_ns = test.getField("nsec_upload");
                        if (!duration_upload_ns.isNull()) {
                            jsonUtil.addString(resultList, "duration_ul",
                                    String.format("%s %s",
                                            format.format(duration_upload_ns.doubleValue() / 1000000000d), // convert ns
                                            // to s
                                            labels.getString("RESULT_DURATION_UNIT")));
                        }

                        if (ndt != null) {
                            final String downloadNdt = format.format(ndt.getField("s2cspd").doubleValue());
                            jsonUtil.addString(resultList, "speed_download_ndt",
                                    String.format("%s %s", downloadNdt, labels.getString("RESULT_DOWNLOAD_UNIT")));

                            final String uploaddNdt = format.format(ndt.getField("c2sspd").doubleValue());
                            jsonUtil.addString(resultList, "speed_upload_ndt",
                                    String.format("%s %s", uploaddNdt, labels.getString("RESULT_UPLOAD_UNIT")));

                            // final String pingNdt =
                            // format.format(ndt.getField("avgrtt").doubleValue());
                            // jsonUtil.addString(resultList, "ping_ndt",
                            // String.format("%s %s", pingNdt,
                            // labels.getString("RESULT_PING_UNIT")));
                        }

                        jsonUtil.addString(resultList, "server_name", test.getField("server_name"));
                        jsonUtil.addString(resultList, "plattform", test.getField("plattform"));
                        jsonUtil.addString(resultList, "os_version", test.getField("os_version"));
                        jsonUtil.addString(resultList, "model", test.getField("model_fullname"));
                        jsonUtil.addString(resultList, "client_name", test.getField("client_name"));
                        jsonUtil.addString(resultList, "client_software_version",
                                test.getField("client_software_version"));
                        final String encryption = test.getField("encryption").toString();

                        if (encryption != null) {
                            jsonUtil.addString(resultList, "encryption",
                                    "NONE".equals(encryption) ? labels.getString("key_encryption_false")
                                            : labels.getString("key_encryption_true"));
                        }

                        jsonUtil.addString(resultList, "client_version", test.getField("client_version"));

                        jsonUtil.addString(resultList, "duration", String.format("%d %s",
                                test.getField("duration").intValue(), labels.getString("RESULT_DURATION_UNIT")));

                        // number of threads for download-test
                        final Field num_threads = test.getField("num_threads");
                        if (!num_threads.isNull()) {
                            jsonUtil.addInt(resultList, "num_threads", num_threads);
                        }

                        // number of threads for upload-test
                        final Field num_threads_ul = test.getField("num_threads_ul");
                        if (!num_threads_ul.isNull()) {
                            jsonUtil.addInt(resultList, "num_threads_ul", num_threads_ul);
                        } else if (!num_threads.isNull()) {
                            jsonUtil.addInt(resultList, "num_threads_ul", num_threads);
                        }

                        // dz 2013-11-09 removed UUID from details as users might get confused by two
                        // ids;
                        // jsonUtil.addString(resultList, "uuid", String.format("T%s", test.getField("uuid")));

                        final Field openTestUUIDField = test.getField("open_test_uuid");
                        if (!openTestUUIDField.isNull())
                            jsonUtil.addString(resultList, "open_test_uuid",
                                    String.format("O%s", openTestUUIDField));

                        // tag
                        final Field tag = test.getField("tag");
                        if (!tag.isNull()) {
                            jsonUtil.addString(resultList, "tag", tag);
                        }

                        if (ndt != null) {
                            jsonUtil.addString(resultList, "ndt_details_main", ndt.getField("main"));
                            jsonUtil.addString(resultList, "ndt_details_stat", ndt.getField("stat"));
                            jsonUtil.addString(resultList, "ndt_details_diag", ndt.getField("diag"));
                        }

                        AdvertisedSpeedOptionUtil.expandWithAdvertisedSpeedStatus(jsonUtil, this, test,
                                resultList, format);

                        // Jitter and packet loss
                        JSONObject resultJPL = new JSONObject();
                        resultJPL.put("voip_objective_bits_per_sample",
                                test.getField("voip_objective_bits_per_sample").longValue());
                        resultJPL.put("voip_result_out_long_seq",
                                test.getField("voip_result_out_long_seq").longValue());
                        resultJPL.put("duration_ns", test.getField("duration_ns").longValue());
                        resultJPL.put("voip_result_in_sequence_error",
                                test.getField("voip_result_in_sequence_error").longValue());
                        resultJPL.put("voip_objective_out_port",
                                test.getField("voip_objective_out_port").longValue());
                        resultJPL.put("voip_objective_payload",
                                test.getField("voip_objective_payload").longValue());
                        resultJPL.put("voip_objective_call_duration",
                                test.getField("voip_objective_call_duration").longValue());
                        resultJPL.put("voip_result_out_short_seq",
                                test.getField("voip_result_out_short_seq").longValue());
                        resultJPL.put("voip_objective_sample_rate",
                                test.getField("voip_objective_sample_rate").longValue());
                        resultJPL.put("voip_result_out_mean_jitter",
                                test.getField("voip_result_out_mean_jitter").longValue());
                        resultJPL.put("voip_result_out_num_packets",
                                test.getField("voip_result_out_num_packets").longValue());
                        resultJPL.put("voip_result_status", test.getField("voip_result_status").toString());
                        resultJPL.put("voip_result_in_skew", test.getField("voip_result_in_skew").longValue());
                        resultJPL.put("voip_result_in_max_jitter",
                                test.getField("voip_result_in_max_jitter").longValue());
                        resultJPL.put("voip_result_out_sequence_error",
                                test.getField("voip_result_out_sequence_error").longValue());
                        resultJPL.put("voip_objective_timeout",
                                test.getField("voip_objective_timeout").longValue());
                        resultJPL.put("voip_result_in_num_packets",
                                test.getField("voip_result_in_num_packets").longValue());
                        resultJPL.put("voip_result_in_mean_jitter",
                                test.getField("voip_result_in_mean_jitter").longValue());
                        resultJPL.put("voip_objective_in_port",
                                test.getField("voip_objective_in_port").longValue());
                        resultJPL.put("voip_result_out_max_jitter",
                                test.getField("voip_result_out_max_jitter").longValue());
                        resultJPL.put("voip_result_in_max_delta",
                                test.getField("voip_result_in_max_delta").longValue());
                        resultJPL.put("voip_result_in_long_seq",
                                test.getField("voip_result_in_long_seq").longValue());
                        resultJPL.put("voip_result_out_max_delta",
                                test.getField("voip_result_out_max_delta").longValue());
                        resultJPL.put("start_time_ns", test.getField("start_time_ns").longValue());
                        resultJPL.put("voip_objective_delay",
                                test.getField("voip_objective_delay").longValue());
                        resultJPL.put("voip_result_in_short_seq",
                                test.getField("voip_result_in_short_seq").longValue());
                        resultJPL.put("voip_result_out_skew",
                                test.getField("voip_result_out_skew").longValue());
                        resultJPL.put("voip_result_jitter", test.getField("voip_result_jitter").toString());
                        resultJPL.put("voip_result_packet_loss",
                                test.getField("voip_result_packet_loss").toString());

                        // check if it is number or no.
                        try {
                            double jitter = Double.parseDouble(test.getField("voip_result_jitter").toString());
                            resultJPL.put("classification_jitter",
                                    Classification.classifyJitter(classification.THRESHOLD_JITTER, jitter));
                        } catch (Exception e) {
                            resultJPL.put("classification_jitter", -1);
                        }

                        try {
                            double packetLoss = Double.parseDouble(test.getField("voip_result_packet_loss").toString());
                            resultJPL.put("classification_packet_loss",
                                    Classification.classifyPacketLoss(classification.THRESHOLD_PACKET_LOSS, packetLoss));
                        } catch (Exception e) {
                            resultJPL.put("classification_packet_loss", -1);
                        }

                        singleItem.put("jpl", resultJPL);
                        // jsonUtil.addJSONObject(resultList, "jpl", resultJPL);


                        final JsonField reportField = (JsonField) test.getField("additional_report_fields");
                        if (!reportField.isNull()) {
                            final JSONObject reportJson = new JSONObject(reportField.toString());
                            final JSONObject resultReportJson = new JSONObject();
                            final DecimalFormat fmt = (DecimalFormat) DecimalFormat.getInstance(locale);
                            fmt.setMaximumFractionDigits(2);

                            double peak = reportJson.optDouble("peak_up_kbit");
                            if (peak > 0) {
                                resultReportJson.put("peak_up_kbit", String.format("%s %s",
                                        fmt.format(peak / 1000d), labels.getString("RESULT_UPLOAD_UNIT")));
                            }

                            peak = reportJson.optDouble("peak_down_kbit");
                            if (peak > 0) {
                                resultReportJson.put("peak_down_kbit", String.format("%s %s",
                                        fmt.format(peak / 1000d), labels.getString("RESULT_DOWNLOAD_UNIT")));
                            }

                            jsonUtil.addJSONObject(resultList, "additional_report_fields", resultReportJson);
                        }

                        // cells_info
                        if (test != null && test.getUid() != 0L) {
                            JSONObject cellsInfoJson = getCellInfoForTestUid(test.getUid());
                            if (cellsInfoJson != null && cellsInfoJson.has("cells_info")) {
                                resultList.put(cellsInfoJson);
                            }
                        }

                        if (resultList.length() == 0)
                            errorList.addError("ERROR_DB_GET_TESTRESULT_DETAIL");

                        answer.put("testresultdetail", resultList);
                    } else
                        errorList.addError("ERROR_REQUEST_TEST_RESULT_DETAIL_NO_UUID");

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
        }

        answerString = answer.toString();

        long elapsedTime = System.currentTimeMillis() - startTime;
        logger.info("Test results details request from " +clientIpRaw+ " completed " +Long.toString(elapsedTime)+ " ms");

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

    /**
     * @param test
     * @param settings
     * @param conn
     * @return
     * @throws JSONException
     */
    public static JSONObject getGeoLocation(Settings sett, Test test, ResourceBundle settings,
                                            Connection conn, ResourceBundle labels) throws JSONException {
        JSONObject json = new JSONObject();
        // geo-location
        final Field latField = test.getField("geo_lat"); // csv 6
        final Field longField = test.getField("geo_long"); // csv 7
        final Field accuracyField = test.getField("geo_accuracy");
        final Field providerField = test.getField("geo_provider"); // csv 8
        if (!(latField.isNull() || longField.isNull() || accuracyField.isNull())) {
            final double accuracy = accuracyField.doubleValue();
            if (accuracy < Double.parseDouble(sett.getSetting("rmbt_geo_accuracy_detail_limit"))) {
                final StringBuilder geoString = new StringBuilder(
                        Helperfunctions.geoToString(latField.doubleValue(), longField.doubleValue()));

                geoString.append(" (");
                if (!providerField.isNull()) {
                    String provider = providerField.toString().toUpperCase(Locale.US);

                    switch (provider) {
                        case "NETWORK":
                            provider = labels.getString("key_geo_source_network");
                            break;
                        case "GPS":
                            provider = labels.getString("key_geo_source_gps");
                            break;
                    }

                    geoString.append(provider);
                    geoString.append(", ");
                }
                geoString.append(String.format(Locale.US, "+/- %.0f m", accuracy));
                geoString.append(")");
                json.put("location", geoString.toString());

                // also try getting the distance during the test
                final Date clientDate = ((TimestampField) test.getField("client_time")).getDate();
                final long clientTime = clientDate.getTime();
                try {
                    OpenTestResource.LocationGraph locGraph =
                            new OpenTestResource.LocationGraph(test.getUid(), clientTime, conn);
                    if ((locGraph.getTotalDistance() > 0) && locGraph.getTotalDistance() <= Double
                            .parseDouble(sett.getSetting("rmbt_geo_distance_detail_limit"))) {
                        json.put("motion", Math.round(locGraph.getTotalDistance()) + " m");
                    }

                } catch (SQLException ex) {
                    // cannot happen since the test uid has to exist in here
                    logger.error(ex.getMessage());
                }
            }

            // country derived from location
            final Field countryLocationField = test.getField("country_location");
            if (!countryLocationField.isNull()) {
                json.put("country_location", countryLocationField.toString());
            }
        }

        return json;
    }

    public JSONObject getCellInfoForTestUid(Long test_uid) {

        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(CellInfo.SELECT_CELL_INFO_FOT_TESTUID);

            SQLHelper.setLongOrNull(st, 1, test_uid);

            logger.debug(st.toString());
            rs = st.executeQuery();

            while (rs != null && rs.next()) {

                if (jsonObject == null && jsonArray == null) {
                    jsonObject = new JSONObject();
                    jsonArray = new JSONArray();
                    jsonObject.put("cells_info", jsonArray);
                }

                JSONObject jsonItem = new JSONObject();

                jsonItem.put("tstamp", rs.getLong(2));
                jsonItem.put("type", rs.getString(3));
                jsonItem.put("arfcn_number", rs.getInt(4));
                jsonItem.put("band", rs.getInt(5));
                jsonItem.put("band_name", rs.getString(6));
                jsonItem.put("frequency_download", rs.getDouble(7));
                jsonItem.put("frequency_upload", rs.getDouble(8));
                jsonItem.put("bandwidth", rs.getDouble(9));
//                jsonItem.put("band", rs.getInt(5) + " MHz");
//                jsonItem.put("band_name", rs.getString(6));
//                jsonItem.put("frequency_download", rs.getDouble(7) + " MHz");
//                jsonItem.put("frequency_upload", rs.getDouble(8) + " Mhz");
//                jsonItem.put("bandwidth", rs.getDouble(9) + " MHz");

                jsonArray.put(jsonItem);
            }//while

            SQLHelper.closeResultSet(rs);
            SQLHelper.closePreparedStatement(st);

        } catch (final Exception e) {
            logger.error(e.getMessage());
            SQLHelper.closeResultSet(rs);
            SQLHelper.closePreparedStatement(st);
        }

        if (jsonObject != null) {
            logger.debug("cells_info: " + jsonObject.toString());
        } else {
            logger.debug("No cells_info found.");
        }
        return jsonObject;
    }

}
