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

import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.db.*;
import at.alladin.rmbt.shared.db.dao.TestStatDao;
import at.alladin.rmbt.shared.db.fields.*;
import at.alladin.rmbt.shared.db.model.AdvertisedSpeedOption;
import at.alladin.rmbt.shared.db.repository.AdvertisedSpeedOptionRepository;
import at.alladin.rmbt.shared.reporting.AdvancedReporting;
import at.alladin.rmbt.util.BandCalculationUtil;
import com.google.common.net.InetAddresses;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ResultResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ResultResource.class);

    // show new provider in map filter( SPECURE, ONT)
    private final Boolean showNewProviderInMapFilter = CustomerResource.getInstance().showNewProviderInMapFilter();

    // ONT-4704 - [Legacy] CS is not checking the version of the test server
    private final Boolean checkMeasurementServerVersion = CustomerResource.getInstance().checkMeasurementServerVersion();

    private static final String SQL_SECRET_TOKEN =
            "Select secret_key from test_server where server_type in ('RMBT', 'RMBTws') and secret_key IS NOT NULL";
    private static final String SQL_INSERT_TEST_JPL =
            "INSERT INTO test_jpl (test_uid, voip_objective_bits_per_sample, "
                    + "voip_result_out_long_seq, duration_ns, voip_result_in_sequence_error, "
                    + "voip_objective_out_port, voip_objective_payload, voip_objective_call_duration, "
                    + "voip_result_out_short_seq, voip_objective_sample_rate, voip_result_out_mean_jitter, "
                    + "voip_result_out_num_packets, voip_result_status, voip_result_in_skew, voip_result_in_max_jitter, "
                    + "voip_result_out_sequence_error, voip_objective_timeout, voip_result_in_num_packets, "
                    + "voip_result_in_mean_jitter, voip_objective_in_port, voip_result_out_max_jitter, "
                    + "voip_result_in_max_delta, voip_result_in_long_seq, voip_result_out_max_delta, "
                    + "start_time_ns, voip_objective_delay, voip_result_in_short_seq, voip_result_out_skew, voip_result_jitter, voip_result_packet_loss) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_AGG_SIGNAL =
            "WITH agg AS" + " (SELECT array_agg(DISTINCT nt.group_name ORDER BY nt.group_name) agg"
                    + " FROM signal s" + " JOIN network_type nt ON s.network_type_id=nt.uid WHERE test_id=?)"
                    + " SELECT uid FROM agg JOIN network_type nt ON nt.aggregate=agg";

    private static final String SQL_INSERT_TEST_SPEED =
            "INSERT INTO test_speed (test_id, upload, thread, time, bytes) VALUES (?,?,?,?,?)";
    private static final String SQL_INSERT_PING =
            "INSERT INTO ping (test_id, value, value_server, time_ns) " + "VALUES(?,?,?,?)";
    private static final String SQL_MAX_NETWORK_TYPE =
            "SELECT nt.uid" + " FROM signal s" + " JOIN network_type nt ON s.network_type_id=nt.uid"
                    + " WHERE test_id=? ORDER BY nt.technology_order DESC" + " LIMIT 1";
    private static final String SQL_MNC =
            "select n.name as mnc_name from mccmnc2name n where n.mccmnc=?";

    // NT-92 - Filters for operators are active and country specific
    private static final String SELECT_UID_FROM_AS2PROVIDER_FOR_ASN = "SELECT uid FROM as2provider where asn = ?";
    private static final String SELECT_UID_FROM_PROVIDER_FOR_MCCMNC = "SELECT uid from provider where mcc_mnc = ?";
    private static final String SELECT_UID_FROM_MCCMNC2NAME_FOR_MCCMNC = "SELECT uid FROM mccmnc2name where mccmnc = ?";
    private static final String SELECT_MAX_UID_FROM_PROVIDER = "SELECT COALESCE(max(uid),0) from provider";
    private static final String SELECT_MAX_UID_FROM_AS2PROVIDER = "SELECT COALESCE(max(uid),0) from as2provider";
    private static final String SELECT_MAX_UID_FROM_ASN2COUNTRY = "SELECT COALESCE(max(uid),0) from asn2country";
    private static final String SELECT_MAX_UID_FROM_MCCMNC2NAME = "SELECT COALESCE(max(uid),0) from mccmnc2name";
    private static final String SELECT_COUNTRY_FROM_MCC2NAME_FOR_MCC = "SELECT country from mcc2country where mcc = ?";
    private static final String INSERT_INTO_PROVIDER = "INSERT INTO provider(uid, name, shortname, mcc_mnc, map_filter) VALUES(?,?,?,?,?)";
    private static final String INSERT_INTO_AS2PROVIDER = "INSERT INTO as2provider(uid, asn, provider_id) VALUES(?,?,?)";
    private static final String INSERT_INTO_ASN2COUNTRY = "INSERT INTO asn2country(uid, asn, country) VALUES(?,?,?)";
    private static final String INSERT_INTO_MCCMNC2NAME = "INSERT INTO mccmnc2name (uid, mccmnc, valid_from, valid_to, country, name, shortname, use_for_sim, use_for_network, mcc_mnc_network_mapping, comment, mapped_uid) VALUES (?, ?, '0001-01-01 BC', '9999-12-31', ?, ?, ?, true, true, null, 'auto-inserted by control server', null)";


    final static int UNKNOWN = Integer.MIN_VALUE;
    final static Pattern MCC_MNC_PATTERN = Pattern.compile("\\d{3}-\\d+");


    @Post("json")
    public String request(final String entity) {

        logger.debug("rquest: " +entity);
        //final String secret = getContext().getParameters().getFirstValue("RMBT_SECRETKEY");

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();

        logger.info("New test result from" + getIP());

        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                List<String> secretKeys = new LinkedList<>();
                // TODO, for server_type use "client_name"
                try (PreparedStatement ps = conn.prepareStatement(SQL_SECRET_TOKEN)) {
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        secretKeys.add(rs.getString("secret_key"));
                    }

                    // close result set
                    SQLHelper.closeResultSet(rs);

                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }

                final String lang = request.optString("client_language");

                // Load Language Files for Client

                final List<String> langs =
                        Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                }

                // System.out.println(request.toString(4));

                if (conn != null) {
                    boolean oldAutoCommitState = conn.getAutoCommit();
                    conn.setAutoCommit(false);

                    final Test test = new Test(conn);

                    if (request.optString("test_token").length() > 0) {

                        final String[] token = request.getString("test_token").split("_");

                        try {

                            // Check if UUID
                            final UUID testUuid = UUID.fromString(token[0]);

                            final String data = token[0] + "_" + token[1];

                            boolean verificated = false;
                            // check secret keys
                            for (String secretKey : secretKeys) {
                                String hmac = Helperfunctions.calculateHMAC(secretKey, data);
                                if (token[2].length() > 0 && hmac.equals(token[2])) {
                                    verificated = true;
                                    break;
                                }// if
                            }// for

                            if (verificated) {

                                final List<String> clientNames =
                                        Arrays.asList(settings.getString("RMBT_CLIENT_NAME").split(",\\s*"));
                                final List<String> clientVersions =
                                        Arrays.asList(settings.getString("RMBT_VERSION_NUMBER").split(",\\s*"));

                                if (test.getTestByUuid(testUuid) > 0)
                                    // ONT-4704 - [Legacy] CS is not checking the version of the test server
//                                    if (clientNames.contains(request.optString("client_name"))
//                                            && clientVersions.contains(request.optString("client_version"))) {
                                    if (!checkMeasurementServerVersion || (clientNames.contains(request.optString("client_name"))
                                            && clientVersions.contains(request.optString("client_version")))) {

                                        test.setFields(request);

                                        final String networkOperator = request.optString("telephony_network_operator");
                                        if (MCC_MNC_PATTERN.matcher(networkOperator).matches())
                                            test.getField("network_operator").setString(networkOperator);
                                        else
                                            test.getField("network_operator").setString(null);

                                        final String networkSimOperator =
                                                request.optString("telephony_network_sim_operator");
                                        if (MCC_MNC_PATTERN.matcher(networkSimOperator).matches())
                                            test.getField("network_sim_operator").setString(networkSimOperator);
                                        else
                                            test.getField("network_sim_operator").setString(null);

                                        // -- oprav nazvu podla MNC codu --

                                        //
                                        String networkSimOperatorName = request.optString("telephony_network_sim_operator_name");//getMncName(test.getField("network_sim_operator").toString());
                                        if (networkSimOperatorName != null && !networkSimOperatorName.isEmpty()) {
                                            test.getField("network_sim_operator_name").setString(networkSimOperatorName);
                                        } else {
                                            networkSimOperatorName = getMncName(test.getField("network_sim_operator").toString());
                                            if (networkSimOperatorName != null && !networkSimOperatorName.isEmpty()) {
                                                test.getField("network_sim_operator_name").setString(networkSimOperatorName);
                                            }
                                        }

                                        final String s2 = getMncName(test.getField("network_operator").toString());
                                        if (!s2.isEmpty()) {
                                            test.getField("network_operator_name").setString(s2);
                                        }

                                        // RMBTClient Info

                                        final String ipLocalRaw = request.optString("test_ip_local", null);
                                        if (ipLocalRaw != null) {
                                            final InetAddress ipLocalAddress = InetAddresses.forString(ipLocalRaw);
                                            // original address (not filtered)
                                            test.getField("client_ip_local")
                                                    .setString(InetAddresses.toAddrString(ipLocalAddress));
                                            // anonymized local address
                                            final String ipLocalAnonymized = Helperfunctions.anonymizeIp(ipLocalAddress);
                                            test.getField("client_ip_local_anonymized").setString(ipLocalAnonymized);
                                            // type of local ip
                                            test.getField("client_ip_local_type")
                                                    .setString(Helperfunctions.IpType(ipLocalAddress));
                                            // public ip
                                            final InetAddress ipPublicAddress =
                                                    InetAddresses.forString(test.getField("client_public_ip").toString());
                                            test.getField("nat_type")
                                                    .setString(Helperfunctions.getNatType(ipLocalAddress, ipPublicAddress));
                                        }

                                        final String ipServer = request.optString("test_ip_server", null);
                                        if (ipServer != null) {
                                            final InetAddress testServerInetAddress = InetAddresses.forString(ipServer);
                                            test.getField("server_ip")
                                                    .setString(InetAddresses.toAddrString(testServerInetAddress));
                                        }

                                        // log IP address
                                        final String ipSource = getIP();
                                        test.getField("source_ip").setString(ipSource);

                                        // log anonymized address
                                        try {
                                            final InetAddress ipSourceIP = InetAddress.getByName(ipSource);
                                            final String ipSourceAnonymized = Helperfunctions.anonymizeIp(ipSourceIP);
                                            test.getField("source_ip_anonymized").setString(ipSourceAnonymized);
                                        } catch (UnknownHostException e) {
                                            logger.error("Exception thrown:" + e.getMessage());
                                        }

                                        // Additional Info

                                        //////////////////////////////////////////////////
                                        // extended test stats:
                                        //////////////////////////////////////////////////
                                        final TestStat extendedTestStat =
                                                TestStat.checkForSubmittedTestStats(request, test.getUid());
                                        if (extendedTestStat != null) {
                                            final TestStatDao testStatDao = new TestStatDao(conn);
                                            testStatDao.save(extendedTestStat);
                                        }

                                        //////////////////////////////////////////////////
                                        // advertised speed option:
                                        //////////////////////////////////////////////////
                                        final Long advSpdOptionId = request.optLong("adv_spd_option_id");
                                        if (advSpdOptionId != null) {
                                            final AdvertisedSpeedOptionRepository repo =
                                                    new AdvertisedSpeedOptionRepository(conn);
                                            final AdvertisedSpeedOption advSpd = repo.getByUid(advSpdOptionId);
                                            if (advSpd != null) {
                                                ((LongField) test.getField("adv_spd_option_id")).setValue(advSpdOptionId);
                                                test.getField("adv_spd_option_name").setString(advSpd.getName());
                                            }

                                            if (request.has("adv_spd_up_kbit")) {
                                                ((LongField) test.getField("adv_spd_up_kbit"))
                                                        .setValue(request.getLong("adv_spd_up_kbit"));
                                            }
                                            if (request.has("adv_spd_down_kbit")) {
                                                ((LongField) test.getField("adv_spd_down_kbit"))
                                                        .setValue(request.getLong("adv_spd_down_kbit"));
                                            }
                                        }

                                        //////////////////////////////////////////////////
                                        // jitter and packet loss
                                        //////////////////////////////////////////////////

                                        JSONObject jitterData = request.optJSONObject("jpl");
                                        if (jitterData != null) {

                                            // add data into DB
                                            PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TEST_JPL);

                                            ps.setObject(1, testUuid);
                                            ps.setLong(2, jitterData.optLong("voip_objective_bits_per_sample"));
                                            ps.setLong(3, jitterData.optLong("voip_result_out_long_seq"));
                                            ps.setLong(4, jitterData.optLong("duration_ns"));
                                            ps.setLong(5, jitterData.optLong("voip_result_in_sequence_error"));
                                            ps.setLong(6, jitterData.optLong("voip_objective_out_port"));
                                            ps.setLong(7, jitterData.optLong("voip_objective_payload"));
                                            Long voip_objective_call_duration = jitterData.optLong("voip_objective_call_duration");
                                            ps.setLong(8, voip_objective_call_duration);
                                            ps.setLong(9, jitterData.optLong("voip_result_out_short_seq"));
                                            ps.setLong(10, jitterData.optLong("voip_objective_sample_rate"));
                                            Long voip_result_out_mean_jitter = jitterData.optLong("voip_result_out_mean_jitter", -1l);
                                            ps.setLong(11, voip_result_out_mean_jitter);
                                            Long voip_result_out_num_packets = jitterData.optLong("voip_result_out_num_packets");
                                            ps.setLong(12, voip_result_out_num_packets);
                                            ps.setLong(13, jitterData.optLong("voip_result_status"));
                                            ps.setLong(14, jitterData.optLong("voip_result_in_skew"));
                                            ps.setLong(15, jitterData.optLong("voip_result_in_max_jitter"));
                                            ps.setLong(16, jitterData.optLong("voip_result_out_sequence_error"));
                                            ps.setLong(17, jitterData.optLong("voip_objective_timeout"));
                                            Long voip_result_in_num_packets = jitterData.optLong("voip_result_in_num_packets");
                                            ps.setLong(18, voip_result_in_num_packets);
                                            Long voip_result_in_mean_jitter = jitterData.optLong("voip_result_in_mean_jitter", -1l);
                                            ps.setLong(19, voip_result_in_mean_jitter);
                                            ps.setLong(20, jitterData.optLong("voip_objective_in_port"));
                                            ps.setLong(21, jitterData.optLong("voip_result_out_max_jitter"));
                                            ps.setLong(22, jitterData.optLong("voip_result_in_max_delta"));
                                            ps.setLong(23, jitterData.optLong("voip_result_in_long_seq"));
                                            ps.setLong(24, jitterData.optLong("voip_result_out_max_delta"));
                                            ps.setLong(25, jitterData.optLong("start_time_ns"));
                                            Long voip_objective_delay = jitterData.optLong("voip_objective_delay");
                                            ps.setLong(26, voip_objective_delay);
                                            ps.setLong(27, jitterData.optLong("voip_result_in_short_seq"));
                                            ps.setLong(28, jitterData.optLong("voip_result_out_skew"));
                                            ps.setString(29, calculateMeanJitterInMms(voip_result_in_mean_jitter,
                                                    voip_result_out_mean_jitter));
                                            ps.setString(30, calculateMeanPacketLossInPercent(voip_objective_delay,
                                                    voip_objective_call_duration,
                                                    voip_result_in_num_packets, voip_result_out_num_packets));

                                            logger.debug(ps.toString());
                                            ps.executeUpdate();

                                        }

                                        //////////////////////////////////////////////////
                                        // ONT-1310 - 5G support in Android legacy App - standalone
                                        int networkType = test.getField("network_type").intValue();
                                        final String telephonyNrConnection = request.optString("telephony_nr_connection");
                                        if (telephonyNrConnection != null && !telephonyNrConnection.isEmpty()) {
                                            if ((networkType == 19 || networkType == 13) && telephonyNrConnection.toUpperCase().compareTo("NSA") == 0) {
                                                networkType = 41;
                                                ((IntField) test.getField("network_type")).setValue(networkType);
                                            } else if ((networkType == 19 || networkType == 13) && telephonyNrConnection.toUpperCase().compareTo("AVAILABLE") == 0) {
                                                networkType = 40;
                                                ((IntField) test.getField("network_type")).setValue(networkType);
                                            }
                                        }

                                        // exclude cli -> ONT-623 - High speed measurements: display results higher than 10Gbps
                                        if (networkType != 97) {

                                            JSONArray speedData = request.optJSONArray("speed_detail");

                                            if (speedData != null && !test.hasError()) {
                                                final PreparedStatement psSpeed = conn.prepareStatement(SQL_INSERT_TEST_SPEED);
                                                psSpeed.setLong(1, test.getUid());
                                                for (int i = 0; i < speedData.length(); i++) {

                                                    final JSONObject item = speedData.getJSONObject(i);

                                                    final String direction = item.optString("direction");
                                                    if (direction != null
                                                            && (direction.equals("download") || direction.equals("upload"))) {
                                                        psSpeed.setBoolean(2, direction.equals("upload"));
                                                        psSpeed.setInt(3, item.optInt("thread"));
                                                        psSpeed.setLong(4, item.optLong("time"));
                                                        psSpeed.setLong(5, item.optLong("bytes"));

                                                        logger.debug(psSpeed.toString());
                                                        psSpeed.executeUpdate();
                                                    }
                                                }
                                            }
                                        }

                                        final JSONArray pingData = request.optJSONArray("pings");

                                        if (pingData != null && !test.hasError()) {
                                            final PreparedStatement psPing = conn.prepareStatement(SQL_INSERT_PING);
                                            psPing.setLong(1, test.getUid());

                                            final List<Long> pingList = new ArrayList<>();

                                            for (int i = 0; i < pingData.length(); i++) {

                                                final JSONObject pingDataItem = pingData.getJSONObject(i);

                                                long valueClient = pingDataItem.optLong("value", -1);
                                                if (valueClient >= 0) {
                                                    // pingList.add(valueClient);

                                                    psPing.setLong(2, valueClient);
                                                } else {
                                                    psPing.setNull(2, Types.BIGINT);
                                                }

                                                long valueServer = pingDataItem.optLong("value_server", -1);
                                                if (valueServer >= 0) {
                                                    pingList.add(valueServer); // use server value for ping variance
                                                    // calculation

                                                    psPing.setLong(3, valueServer);
                                                } else {
                                                    psPing.setNull(3, Types.BIGINT);
                                                }

                                                long timeNs = pingDataItem.optLong("time_ns", -1);
                                                if (timeNs >= 0)
                                                    psPing.setLong(4, timeNs);
                                                else
                                                    psPing.setNull(4, Types.BIGINT);

                                                logger.debug(psPing.toString());
                                                psPing.executeUpdate();

                                                // set ping variance
                                                ((DoubleField) test.getField("ping_variance"))
                                                        .setValue(caluclatePingVariance(pingList));
                                            }
                                        }

                                        final JSONArray geoData = request.optJSONArray("geoLocations");

                                        if (geoData != null && !test.hasError())
                                            for (int i = 0; i < geoData.length(); i++) {

                                                final JSONObject geoDataItem = geoData.getJSONObject(i);

                                                if (geoDataItem.optLong("tstamp", 0) != 0
                                                        && geoDataItem.optDouble("geo_lat", 0) != 0
                                                        && geoDataItem.optDouble("geo_long", 0) != 0) {

                                                    final GeoLocation geoloc = new GeoLocation(conn);

                                                    geoloc.setTest_id(test.getUid());

                                                    final long clientTime = geoDataItem.optLong("tstamp");
                                                    final Timestamp tstamp =
                                                            java.sql.Timestamp.valueOf(new Timestamp(clientTime).toString());

                                                    geoloc.setTime(tstamp, test.getField("timezone").toString());
                                                    geoloc.setAccuracy((float) geoDataItem.optDouble("accuracy", 0));
                                                    geoloc.setAltitude(geoDataItem.optDouble("altitude", 0));
                                                    geoloc.setBearing((float) geoDataItem.optDouble("bearing", 0));
                                                    geoloc.setSpeed((float) geoDataItem.optDouble("speed", 0));
                                                    geoloc.setProvider(geoDataItem.optString("provider", ""));
                                                    geoloc.setGeo_lat(geoDataItem.optDouble("geo_lat", 0));
                                                    geoloc.setGeo_long(geoDataItem.optDouble("geo_long", 0));
                                                    geoloc.setTime_ns(geoDataItem.optLong("time_ns", 0));

                                                    geoloc.storeLocation();

                                                    // Store Last Geolocation as
                                                    // Testlocation
                                                    if (i == geoData.length() - 1) {
                                                        if (geoDataItem.has("geo_lat"))
                                                            test.getField("geo_lat").setField(geoDataItem);

                                                        if (geoDataItem.has("geo_long"))
                                                            test.getField("geo_long").setField(geoDataItem);

                                                        if (geoDataItem.has("accuracy"))
                                                            test.getField("geo_accuracy").setField(geoDataItem);

                                                        if (geoDataItem.has("provider"))
                                                            test.getField("geo_provider").setField(geoDataItem);
                                                    }

                                                    if (geoloc.hasError()) {
                                                        errorList.addError(geoloc.getError());
                                                        break;
                                                    }

                                                }

                                            }

                                        final JSONArray cellData = request.optJSONArray("cellLocations");

                                        if (cellData != null && !test.hasError())
                                            for (int i = 0; i < cellData.length(); i++) {

                                                final JSONObject cellDataItem = cellData.getJSONObject(i);

                                                final Cell_location cellloc = new Cell_location(conn);

                                                cellloc.setTest_id(test.getUid());

                                                final long clientTime = cellDataItem.optLong("time");
                                                final Timestamp tstamp =
                                                        java.sql.Timestamp.valueOf(new Timestamp(clientTime).toString());

                                                cellloc.setTime(tstamp, test.getField("timezone").toString());

                                                cellloc.setTime_ns(cellDataItem.optLong("time_ns", 0));

                                                cellloc.setLocation_id(cellDataItem.optInt("location_id", 0));
                                                cellloc.setArea_code(cellDataItem.optInt("area_code", 0));

                                                cellloc.setPrimary_scrambling_code(
                                                        cellDataItem.optInt("primary_scrambling_code", 0));

                                                cellloc.storeLocation();

                                                if (cellloc.hasError()) {
                                                    errorList.addError(cellloc.getError());
                                                    break;
                                                }

                                            }

                                        int signalStrength = Integer.MAX_VALUE; // measured as RSSI (GSM,UMTS,Wifi)
                                        int lteRsrp = Integer.MAX_VALUE; // signal strength measured as RSRP
                                        int lteRsrq = Integer.MAX_VALUE; // signal quality of LTE measured as RSRQ
                                        int linkSpeed = UNKNOWN;

                                        final JSONArray signalData = request.optJSONArray("signals");

                                        if (signalData != null && !test.hasError()) {

                                            for (int i = 0; i < signalData.length(); i++) {

                                                final JSONObject signalDataItem = signalData.getJSONObject(i);

                                                final Signal signal = new Signal(conn);

                                                signal.setTest_id(test.getUid());

                                                final long clientTime = signalDataItem.optLong("time");
                                                final Timestamp tstamp =
                                                        java.sql.Timestamp.valueOf(new Timestamp(clientTime).toString());

                                                signal.setTime(tstamp, test.getField("timezone").toString());

                                                final int thisNetworkType = signalDataItem.optInt("network_type_id", 0);
                                                signal.setNetwork_type_id(thisNetworkType);

                                                final int thisSignalStrength =
                                                        signalDataItem.optInt("signal_strength", UNKNOWN);
                                                if (thisSignalStrength != UNKNOWN)
                                                    signal.setSignal_strength(thisSignalStrength);
                                                signal
                                                        .setGsm_bit_error_rate(signalDataItem.optInt("gsm_bit_error_rate", 0));
                                                final int thisLinkSpeed = signalDataItem.optInt("wifi_link_speed", 0);
                                                signal.setWifi_link_speed(thisLinkSpeed);
                                                final int rssi = signalDataItem.optInt("wifi_rssi", UNKNOWN);
                                                if (rssi != UNKNOWN)
                                                    signal.setWifi_rssi(rssi);

                                                lteRsrp = signalDataItem.optInt("lte_rsrp", UNKNOWN);
                                                lteRsrq = signalDataItem.optInt("lte_rsrq", UNKNOWN);
                                                final int lteRssnr = signalDataItem.optInt("lte_rssnr", UNKNOWN);
                                                final int lteCqi = signalDataItem.optInt("lte_cqi", UNKNOWN);
                                                final long timeNs = signalDataItem.optLong("time_ns", UNKNOWN);
                                                signal.setLte_rsrp(lteRsrp);
                                                signal.setLte_rsrq(lteRsrq);
                                                signal.setLte_rssnr(lteRssnr);
                                                signal.setLte_cqi(lteCqi);
                                                signal.setTime_ns(timeNs);

                                                // ONT-1310 - 5G in Android legacy App - standalone
                                                if((networkType == 20 || networkType == 41) &&
                                                        (thisSignalStrength != Integer.MAX_VALUE && thisSignalStrength != UNKNOWN && thisSignalStrength != 0)) {
                                                    signal.setSs_rsrp(thisSignalStrength);
                                                }

                                                signal.storeSignal();

                                                if (networkType == 99) // wlan
                                                {
                                                    if (rssi < signalStrength && rssi != UNKNOWN)
                                                        signalStrength = rssi;
                                                } else if (thisSignalStrength < signalStrength
                                                        && thisSignalStrength != UNKNOWN)
                                                    signalStrength = thisSignalStrength;

                                                if (thisLinkSpeed != 0
                                                        && (linkSpeed == UNKNOWN || thisLinkSpeed < linkSpeed))
                                                    linkSpeed = thisLinkSpeed;

                                                if (signal.hasError()) {
                                                    errorList.addError(signal.getError());
                                                    break;
                                                }

                                            }
                                            // set rssi value (typically GSM,UMTS, but also old LTE-phones)
                                            if (signalStrength != Integer.MAX_VALUE && signalStrength != UNKNOWN
                                                    && signalStrength != 0) // 0 dBm is out of range
                                                ((IntField) test.getField("signal_strength")).setValue(signalStrength);
                                            // set rsrp value (typically LTE)
                                            if (lteRsrp != Integer.MAX_VALUE && lteRsrp != UNKNOWN && lteRsrp != 0) // 0
                                                // dBm
                                                // is
                                                // out
                                                // of
                                                // range
                                                ((IntField) test.getField("lte_rsrp")).setValue(lteRsrp);
                                            // set rsrq value (LTE)
                                            if (lteRsrq != Integer.MAX_VALUE && lteRsrq != UNKNOWN)
                                                ((IntField) test.getField("lte_rsrq")).setValue(lteRsrq);

                                            if (linkSpeed != Integer.MAX_VALUE && linkSpeed != UNKNOWN)
                                                ((IntField) test.getField("wifi_link_speed")).setValue(linkSpeed);

                                            // ONT-1310 - 5G in Android legacy App - standalone
                                            if((networkType == 20 || networkType == 41) && (signalStrength != Integer.MAX_VALUE && signalStrength != UNKNOWN && signalStrength != 0)) {
                                                ((IntField) test.getField("ss_rsrp")).setValue(signalStrength);
                                            }
                                        }

                                        // new functionality -> cell_info
                                        final JSONArray cellsInfo = request.optJSONArray("cells_info");

                                        if (cellsInfo != null && !test.hasError()) {

                                            for (int i = 0; i < cellsInfo.length(); i++) {

                                                final JSONObject cellInfoItem = cellsInfo.getJSONObject(i);

                                                final CellInfo cellInfo = new CellInfo(conn);

                                                // test_uid
                                                cellInfo.setTest_uid(test.getUid());

                                                // time
                                                Long time = cellInfoItem.getLong("tstamp");
                                                cellInfo.setTime(time);

                                                // type
                                                // EARFCN / UARFCN / ARFCN
                                                String type = cellInfoItem.getString("type").toUpperCase();
                                                cellInfo.setType(type.length() > 20 ? type.substring(0, 19) : type);

                                                // arfcn_number
                                                Integer arfcn_number = cellInfoItem.getInt("arfcn_number");
                                                cellInfo.setArfcnNumber(arfcn_number);

                                                // now calculate other values
                                                BandCalculationUtil.FrequencyInformation frequencyInformation;
                                                switch (type) {
                                                    case "EARFCN": {
                                                        frequencyInformation = BandCalculationUtil.getBandFromEarfcn(arfcn_number);
                                                        break;
                                                    }
                                                    case "UARFCN": {
                                                        frequencyInformation = BandCalculationUtil.getBandFromUarfcn(arfcn_number);
                                                        break;
                                                    }
                                                    case "ARFCN": {
                                                        frequencyInformation = BandCalculationUtil.getBandFromArfcn(arfcn_number);
                                                        break;
                                                    }
                                                    default: {
                                                        frequencyInformation = null;
                                                        logger.error("Illegal band type: " + type);
                                                    }
                                                }

                                                if (frequencyInformation != null) {

                                                    // band
                                                    Integer band = frequencyInformation.getBand();
                                                    cellInfo.setResultBand(band);

                                                    // band name
                                                    String bandName = frequencyInformation.getInformalName();
                                                    cellInfo.setResultBandName(bandName.length() > 50 ? bandName.substring(0, 49) : bandName);

                                                    // frequency download
                                                    Double frequencyDownload = frequencyInformation.getFrequencyDL();
                                                    cellInfo.setResultFrequencyDownload(frequencyDownload);

                                                    // frequency upload
                                                    Double frequencyUpload = frequencyInformation.getFrequencyULfromDL();
                                                    cellInfo.setResultFrequencyUpload(frequencyUpload);

                                                    // bandwidth
                                                    Double bandwidth = frequencyInformation.getBandwidth();
                                                    cellInfo.setResultBandwidth(bandwidth);

                                                    // store data into database
                                                    cellInfo.storeCellInfo();

                                                    // check error
                                                    if (cellInfo.hasError()) {
                                                        errorList.addError(cellInfo.getError());
                                                        break;
                                                    }

                                                } else {
                                                    logger.error("Illegal band type: " + type);
                                                }//if

                                            }//for

                                        }//if

                                        // new column loop_measurement
                                        final Object loopUuidString = request.opt("loop_uuid");
                                        if(loopUuidString != null && loopUuidString instanceof String
                                                && ((String)loopUuidString).isEmpty() == false && UUID.fromString(((String)loopUuidString)).toString().equals(loopUuidString)) {
                                            test.getField("loop_measurement").setString(((String)loopUuidString));
                                        }


                                        // use max network type
                                        final PreparedStatement psMaxNetworkType =
                                                conn.prepareStatement(SQL_MAX_NETWORK_TYPE);
                                        psMaxNetworkType.setLong(1, test.getUid());
                                        logger.debug(psMaxNetworkType.toString());
                                        if (psMaxNetworkType.execute()) {
                                            final ResultSet rs = psMaxNetworkType.getResultSet();
                                            if (rs.next()) {
                                                final int maxNetworkType = rs.getInt("uid");

                                                // ONT-1613 - Wrong 5G NSA mode in the results
                                                if (maxNetworkType != 0) {
                                                    if (telephonyNrConnection != null && !telephonyNrConnection.isEmpty() && telephonyNrConnection.toUpperCase().compareTo("NSA") == 0 && (maxNetworkType == 19 || maxNetworkType == 13)) {
                                                        networkType = 41;
                                                        ((IntField) test.getField("network_type")).setValue(networkType);
                                                    } else if (telephonyNrConnection != null && !telephonyNrConnection.isEmpty() && telephonyNrConnection.toUpperCase().compareTo("AVAILABLE") == 0 && (maxNetworkType == 19 || maxNetworkType == 13)) {
                                                        networkType = 40;
                                                        ((IntField) test.getField("network_type")).setValue(networkType);
                                                    } else {
                                                        ((IntField) test.getField("network_type")).setValue(maxNetworkType);
                                                    }
                                                }
//                                                if (maxNetworkType != 0)
//                                                    ((IntField) test.getField("network_type")).setValue(maxNetworkType);
                                            }

                                            // close result set
                                            SQLHelper.closeResultSet(rs);

                                        }

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(psMaxNetworkType);

                                        /*
                                         * check for different types (e.g. 2G/3G)
                                         */
                                        final PreparedStatement psAgg = conn.prepareStatement(SQL_AGG_SIGNAL);
                                        psAgg.setLong(1, test.getUid());
                                        logger.debug(psAgg.toString());
                                        if (psAgg.execute()) {
                                            final ResultSet rs = psAgg.getResultSet();
                                            if (rs.next()) {
                                                final int newNetworkType = rs.getInt("uid");

                                                // ONT-1613 - Wrong 5G NSA mode in the results
                                                if (telephonyNrConnection != null && !telephonyNrConnection.isEmpty() && telephonyNrConnection.toUpperCase().compareTo("NSA") == 0
                                                        && (networkType == 19 || networkType == 13 || networkType == 41)) {
                                                    networkType = 41;
                                                    ((IntField) test.getField("network_type")).setValue(networkType);
                                                } else if (telephonyNrConnection != null && !telephonyNrConnection.isEmpty() && telephonyNrConnection.toUpperCase().compareTo("AVAILABLE") == 0
                                                        && (networkType == 19 || networkType == 13 || networkType == 40)) {
                                                    networkType = 40;
                                                    ((IntField) test.getField("network_type")).setValue(networkType);
                                                } else if (newNetworkType != 0) {
                                                    ((IntField) test.getField("network_type")).setValue(newNetworkType);
                                                }
//                                                    if (newNetworkType != 0)
//                                                        ((IntField) test.getField("network_type")).setValue(newNetworkType);

                                            }

                                            // close result set
                                            SQLHelper.closeResultSet(rs);

                                        }

                                        // close prepared statement
                                        SQLHelper.closePreparedStatement(psAgg);

                                        if (test.getField("network_type").intValue() <= 0)
                                            errorList.addError("ERROR_NETWORK_TYPE");

                                        // ONT-623 - High speed measurements: display results higher than 10Gbps
//                                        final IntField downloadField = (IntField) test.getField("speed_download");
//                                        if (downloadField.isNull() || downloadField.intValue() <= 0
//                                                || downloadField.intValue() > 10000000) // 10 gbit/s limit
//                                            errorList.addError("ERROR_DOWNLOAD_INSANE");
//
//                                        final IntField upField = (IntField) test.getField("speed_upload");
//                                        if (upField.isNull() || upField.intValue() <= 0
//                                                || upField.intValue() > 10000000) // 10 gbit/s limit
//                                            errorList.addError("ERROR_UPLOAD_INSANE");

                                        // clients still report eg: "test_ping_shortest":9195040 (note the 'test_'
                                        // prefix there!)
                                        final LongField pingField = (LongField) test.getField("ping_shortest");
                                        if (pingField.isNull() || pingField.longValue() <= 0
                                                || pingField.longValue() > 60000000000L) // 1 min limit
                                            errorList.addError("ERROR_PING_INSANE");

                                        if (errorList.isEmpty())
                                            test.getField("status").setString("FINISHED");
                                        else
                                            test.getField("status").setString("ERROR");

                                        final AdvancedReporting advancedReporting = getAdvancedReporting();
                                        final JSONObject reportField =
                                                advancedReporting.generateSpeedtestAdvancedReport(test, request);
                                        if (reportField != null && reportField.length() > 0) {
                                            final Field f = test.getField("additional_report_fields");
                                            if (f != null) {
                                                ((JsonField) f).setString(reportField.toString());
                                            }
                                        }

                                        String resultURL = settings.getString("RMBT_RESULT_URL");
                                        //String resultURL = settings.getString("RMBT_URL") + "/" + lang + "/history/";
                                        //System.out.println("RMBT_RESULT_URL: " + resultURL);
                                        answer.put("RMBT_RESULT_URL", resultURL);

                                        test.storeTestResults(false);

                                        conn.commit();
                                        conn.setAutoCommit(oldAutoCommitState); // be nice and restore old state TODO: do it in

                                        // NT-92 - Filters for operators are active and country specific
                                        try {
                                            if (networkType == 97 || networkType == 98 || networkType == 99) {

                                                // insert new fixed provider only in case when the client is not in roaming(national, international)
                                                final Long asn = Helperfunctions.getASN(InetAddresses.forString(getIP()));
                                                logger.debug("asn: " + asn);
                                                String asName = null;
                                                String asCountry = null;
                                                if (asn != null) {
                                                    asName = Helperfunctions.getASName(asn);
                                                    logger.debug("asName: " + asName);
                                                    asCountry = Helperfunctions.getAScountry(asn);
                                                    logger.debug("asCountry: " + asCountry);

                                                    if (asName != null && asName.isEmpty() == false && asCountry != null && asCountry.isEmpty() == false) {
                                                        logger.debug("Going to insert new fixed provider... " + asName);
                                                        if (asName.length() > 99) {
                                                            insertNewProviderIfNeeded(asn, asName.substring(0, 99), asCountry, null);
                                                        } else {
                                                            insertNewProviderIfNeeded(asn, asName, asCountry, null);
                                                        }
                                                        logger.debug("Going to insert new fixed provider... DONE");
                                                    }
                                                }
                                            } else if (networkSimOperator != null && networkSimOperator.isEmpty() == false
                                                    && networkSimOperatorName != null && networkSimOperatorName.isEmpty() == false
                                                    && MCC_MNC_PATTERN.matcher(networkSimOperator).matches()) {

                                                // insert new mobile provider only in case when the client is not in roaming(national, international)
                                                logger.debug("Going to insert new mobile provider... " + networkSimOperatorName + " - " + networkSimOperator);
                                                if (networkSimOperatorName.length() > 99) {
                                                    insertNewProviderIfNeeded(null, networkSimOperatorName.substring(0, 99), null, networkSimOperator);
                                                } else {
                                                    insertNewProviderIfNeeded(null, networkSimOperatorName, null, networkSimOperator);
                                                }
                                                logger.debug("Going to insert new mobile provider... DONE");
                                            }
                                        } catch (Exception e) {
                                            logger.error(e.getMessage());
                                        }


                                        if (test.hasError())
                                            errorList.addError(test.getError());

                                    } else
                                        errorList.addError("ERROR_CLIENT_VERSION");
                            } else
                                errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        } catch (final IllegalArgumentException e) {
                            logger.error(e.getMessage());
                            errorList.addError("ERROR_TEST_TOKEN_MALFORMED");
                        } catch (InstantiationException e) {
                            logger.error(e.getMessage());
                        } catch (IllegalAccessException e) {
                            logger.error(e.getMessage());
                        }
                    } else
                        errorList.addError("ERROR_TEST_TOKEN_MISSING");


                    // finally
                } else
                    errorList.addError("ERROR_DB_CONNECTION");

            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSDON Data " + e.toString());
            } catch (final SQLException e) {
                logger.error("Error while storing data " + e.toString());
            }
        } else {
            errorList.addErrorString("Expected request is missing.");
        }

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
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

                        // close result set
                        SQLHelper.closeResultSet(rs);
                    }

                    // close prepared statement
                    SQLHelper.closePreparedStatement(psMnc);
                }
            }
        } catch (final Exception e) {
            logger.error("Error getMncName " + e.toString());
        }
        return result;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

    /**
     * Calculates the variance of the givens pings.
     *
     * @param pings
     * @return The variance as double value
     */
    private Double caluclatePingVariance(List<Long> pings) {
        double pingMean = 0;
        double pingCount = pings.size();

        if (pingCount < 1) {
            return null; // cannot be calculated if there are no pings
        }

        for (long p : pings) {
            pingMean += p;
        }

        pingMean /= pingCount;

        ///

        double pingVariance = 0;

        for (long p : pings) {
            pingVariance += Math.pow((p - pingMean), 2);
        }

        pingVariance /= pingCount;

        return pingVariance;
    }

    public String calculateMeanJitterInMms(Long resultInMeanJitter, Long resultOutMeanJitter) {

        String meanJitterFormattedString = "-";

        try {
            if (resultInMeanJitter != null && resultOutMeanJitter != null && resultInMeanJitter >= 0l && resultOutMeanJitter >= 0l) {
                double meanJitter = (resultInMeanJitter + resultOutMeanJitter) / 2;
                DecimalFormat df = new DecimalFormat("#,##0.00");
                meanJitterFormattedString = df.format(meanJitter / 1000000).replace(',', '.');
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        return meanJitterFormattedString;

    }

    public String calculateMeanPacketLossInPercent(Long objectiveDelay, Long objectiveCallDuration, Long resultInNumPackets, Long resultOutNumPackets) {

        String packetLossStr = "-";

        try {
            int total = 0;
            if ((objectiveDelay != null) && (objectiveDelay != 0)) {
                total = (int) (objectiveCallDuration / objectiveDelay);
            }

            int packetLossDown = (int) (100f * ((float) (total - resultInNumPackets) / (float) total));
            int packetLossUp = (int) (100f * ((float) (total - resultOutNumPackets) / (float) total));
            if ((packetLossDown >= 0) && (packetLossUp >= 0)) {
                double meanPacketLoss = (packetLossDown + packetLossUp) / 2;
                DecimalFormat df = new DecimalFormat("0.0");
                packetLossStr = df.format(meanPacketLoss).replace(',', '.');
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        return packetLossStr;
    }

    private void insertNewProviderIfNeeded(Long asn, String asName, String asCountry, String mccMncCode) {

        logger.debug("Insert new provider if needed. Use following values: ASN -> " + asn +
                ", PROVIDER -> " + asName + ", COUNTRY -> " + asCountry + ", MCCMNC -> " + mccMncCode);

        // db connection
        //final Connection conn;
        final boolean previousValueOfAutocommit;
        try {
            // get db connection
            //conn = DbConnection.getConnection();

            // remember last value of autocommit
            previousValueOfAutocommit = conn.getAutoCommit();

            // turn off autocommit
            conn.setAutoCommit(false);

        } catch (SQLException e) {
            logger.error(e.getMessage());
            return;
        }

        // prepared statement
        PreparedStatement preparedStatement = null;
        // result set
        ResultSet resultSet = null;

        // provider UID
        Long providerUID = null;

        try {

            if (mccMncCode != null && mccMncCode.isEmpty() == false) {
                // mobile providers


                preparedStatement = conn.prepareStatement(SELECT_UID_FROM_PROVIDER_FOR_MCCMNC);
                preparedStatement.setString(1, mccMncCode);

                // execute select
                logger.debug(preparedStatement.toString());
                preparedStatement.execute();

                // get result
                resultSet = preparedStatement.getResultSet();

                // check result
                if (resultSet != null && resultSet.next()) {
                    // get as2provider UID
                    providerUID = resultSet.getLong(1);

                    // close result set
                    SQLHelper.closeResultSet(resultSet);

                    // close prepared statement
                    SQLHelper.closePreparedStatement(preparedStatement);
                }


                // if provider doesn't exist, insert it to the database
                if (providerUID == null) {
                    // prepare select to get max uid from provider
                    preparedStatement = conn.prepareStatement(SELECT_MAX_UID_FROM_PROVIDER);

                    // execute select
                    logger.debug(preparedStatement.toString());
                    preparedStatement.execute();

                    // get result
                    resultSet = preparedStatement.getResultSet();

                    // check result
                    if (resultSet != null && resultSet.next()) {
                        // get provider UID
                        providerUID = resultSet.getLong(1);

                        // close result set
                        SQLHelper.closeResultSet(resultSet);

                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);

                        // prepare insert into provider
                        preparedStatement = conn.prepareStatement(INSERT_INTO_PROVIDER);
                        preparedStatement.setLong(1, ++providerUID);
                        preparedStatement.setString(2, asName);
                        preparedStatement.setString(3, asName);
                        SQLHelper.setStringOrNull(preparedStatement, 4, mccMncCode);
                        if (showNewProviderInMapFilter) {
                            preparedStatement.setBoolean(5, Boolean.TRUE);
                        } else {
                            preparedStatement.setBoolean(5, Boolean.FALSE);
                        }

                        // execute insert
                        logger.debug(preparedStatement.toString());
                        preparedStatement.execute();

                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);
                    }
                }

                // check if mccmnc exists in the table mccmnc2name, if not create new
                preparedStatement = conn.prepareStatement(SELECT_UID_FROM_MCCMNC2NAME_FOR_MCCMNC);
                preparedStatement.setString(1, mccMncCode);

                // execute select
                logger.debug(preparedStatement.toString());
                preparedStatement.execute();

                // get result
                resultSet = preparedStatement.getResultSet();

                // check result
                if (resultSet == null || resultSet.next() == false) {

                    // close prepared statement
                    SQLHelper.closePreparedStatement(preparedStatement);

                    // select max(uid), then do the insert
                    preparedStatement = conn.prepareStatement(SELECT_MAX_UID_FROM_MCCMNC2NAME);

                    // execute select
                    logger.debug(preparedStatement.toString());
                    preparedStatement.execute();

                    // get result
                    resultSet = preparedStatement.getResultSet();

                    // get max uid
                    long mccmnc2nameUID = 0l;
                    if (resultSet != null && resultSet.next()) {
                        mccmnc2nameUID = resultSet.getLong(1);
                    }

                    // close result set
                    SQLHelper.closeResultSet(resultSet);

                    // close prepared statement
                    SQLHelper.closePreparedStatement(preparedStatement);

                    // select country for mcc
                    preparedStatement = conn.prepareStatement(SELECT_COUNTRY_FROM_MCC2NAME_FOR_MCC);
                    preparedStatement.setString(1, mccMncCode.substring(0, 3));

                    // execute select
                    logger.debug(preparedStatement.toString());
                    preparedStatement.execute();

                    // get result
                    resultSet = preparedStatement.getResultSet();

                    String countryCode = null;
                    // get country code
                    if (resultSet != null && resultSet.next()) {
                        countryCode = resultSet.getString(1);

                        // close result set
                        SQLHelper.closeResultSet(resultSet);

                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);
                    } else {
                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);
                    }

                    // do insert into mccmnc2name
                    if (countryCode != null) {
                        // prepare insert into mccmnc2name
                        preparedStatement = conn.prepareStatement(INSERT_INTO_MCCMNC2NAME);
                        preparedStatement.setLong(1, ++mccmnc2nameUID);
                        SQLHelper.setStringOrNull(preparedStatement, 2, mccMncCode);
                        preparedStatement.setString(3, countryCode);
                        SQLHelper.setStringOrNull(preparedStatement, 4, asName);
                        SQLHelper.setStringOrNull(preparedStatement, 5, asName);

                        // execute insert
                        logger.debug(preparedStatement.toString());
                        preparedStatement.execute();

                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);
                    }

                } else {
                    // close result set
                    SQLHelper.closeResultSet(resultSet);

                    // close prepared statement
                    SQLHelper.closePreparedStatement(preparedStatement);
                }

            } else {
                // fixed providers

                // check if ASN is already in database
                preparedStatement = conn.prepareStatement(SELECT_UID_FROM_AS2PROVIDER_FOR_ASN);
                preparedStatement.setLong(1, asn);

                // execute select
                logger.debug(preparedStatement.toString());
                if (preparedStatement.execute()) {

                    // get result
                    resultSet = preparedStatement.getResultSet();

                    // check result
                    if (resultSet == null || resultSet.next() == false) {
                        // asn doesn't exist in the database

                        // close result set
                        SQLHelper.closeResultSet(resultSet);

                        // close prepared statement
                        SQLHelper.closePreparedStatement(preparedStatement);

                        // check if provider already exists in the database
                        if (mccMncCode != null && mccMncCode.isEmpty() == false) {

                            // prepare select
                            preparedStatement = conn.prepareStatement(SELECT_UID_FROM_PROVIDER_FOR_MCCMNC);
                            preparedStatement.setString(1, mccMncCode);

                            // execute select
                            logger.debug(preparedStatement.toString());
                            preparedStatement.execute();

                            // get result
                            resultSet = preparedStatement.getResultSet();

                            // check result
                            if (resultSet != null && resultSet.next()) {
                                // get as2provider UID
                                providerUID = resultSet.getLong(1);

                                // close result set
                                SQLHelper.closeResultSet(resultSet);

                                // close prepared statement
                                SQLHelper.closePreparedStatement(preparedStatement);
                            }
                        }

                        // if provider doesn't exist, insert it to the database
                        if (providerUID == null) {
                            // prepare select to get max uid from provider
                            preparedStatement = conn.prepareStatement(SELECT_MAX_UID_FROM_PROVIDER);

                            // execute select
                            logger.debug(preparedStatement.toString());
                            preparedStatement.execute();

                            // get result
                            resultSet = preparedStatement.getResultSet();

                            // check result
                            if (resultSet != null && resultSet.next()) {
                                // get provider UID
                                providerUID = resultSet.getLong(1);

                                // close result set
                                SQLHelper.closeResultSet(resultSet);

                                // close prepared statement
                                SQLHelper.closePreparedStatement(preparedStatement);

                                // prepare insert into provider
                                preparedStatement = conn.prepareStatement(INSERT_INTO_PROVIDER);
                                preparedStatement.setLong(1, ++providerUID);
                                preparedStatement.setString(2, asName);
                                preparedStatement.setString(3, asName);
                                SQLHelper.setStringOrNull(preparedStatement, 4, mccMncCode);
                                preparedStatement.setBoolean(5, Boolean.TRUE);

                                // execute insert
                                logger.debug(preparedStatement.toString());
                                preparedStatement.execute();

                                // close prepared statement
                                SQLHelper.closePreparedStatement(preparedStatement);
                            }
                        }

                        // do insert into tables: as2provider, asn2country

                        // select max( uid) from as2provider
                        preparedStatement = conn.prepareStatement(SELECT_MAX_UID_FROM_AS2PROVIDER);

                        // execute select
                        logger.debug(preparedStatement.toString());
                        if (preparedStatement.execute()) {

                            Long as2providerUID = 1l;

                            // get result
                            resultSet = preparedStatement.getResultSet();

                            // check result
                            if (resultSet != null && resultSet.next()) {
                                // get as2provider UID
                                as2providerUID = resultSet.getLong(1);

                                // close result set
                                SQLHelper.closeResultSet(resultSet);

                                // close prepared statement
                                SQLHelper.closePreparedStatement(preparedStatement);
                            }

                            // prepare insert into as2provider
                            preparedStatement = conn.prepareStatement(INSERT_INTO_AS2PROVIDER);
                            preparedStatement.setLong(1, ++as2providerUID);
                            preparedStatement.setLong(2, asn);
                            preparedStatement.setLong(3, providerUID);

                            // execute insert
                            logger.debug(preparedStatement.toString());
                            preparedStatement.execute();

                            // close prepared statement
                            SQLHelper.closePreparedStatement(preparedStatement);

                            // select max( uid) from asn2country
                            preparedStatement = conn.prepareStatement(SELECT_MAX_UID_FROM_ASN2COUNTRY);

                            // execute select
                            logger.debug(preparedStatement.toString());
                            if (preparedStatement.execute()) {

                                Long as2countryUID = 1l;

                                // get result
                                resultSet = preparedStatement.getResultSet();
                                if (resultSet != null && resultSet.next()) {
                                    // get asn2country UID
                                    as2countryUID = resultSet.getLong(1);

                                    // close result set
                                    SQLHelper.closeResultSet(resultSet);

                                    // close prepared statement
                                    SQLHelper.closePreparedStatement(preparedStatement);
                                }

                                // prepare insert into asn2country
                                preparedStatement = conn.prepareStatement(INSERT_INTO_ASN2COUNTRY);
                                preparedStatement.setLong(1, ++as2countryUID);
                                preparedStatement.setLong(2, asn);
                                preparedStatement.setString(3, asCountry.toLowerCase());

                                // execute insert
                                logger.debug(preparedStatement.toString());
                                preparedStatement.execute();

                                // close prepared statement
                                SQLHelper.closePreparedStatement(preparedStatement);

                                // finally inserted all 3 records: provider, as2provider, asn2country

                                // do commit
                                logger.debug("Commit inserts into tables: provider, as2provider and as2country...");
                                conn.commit();
                                logger.debug("Commit inserts into tables: provider, as2provider and as2country... DONE");

                            } else {
                                throw new SQLException("Select max(uid) from asn2country failed!");
                            }//if - SELECT max(uid) from asn2country

                        } else {
                            throw new SQLException("Select max(uid) from as2provider failed!");
                        }//if - SELECT max(uid) from as2provider

                    } else {
                        logger.debug("ASN " + asn + " already exists in the database. No insert is needed...");
                    }//if - resultSet != null

                }// if - SELECT uid FROM as2provider where asn = ?

            }

        } catch (SQLException e) {
            logger.error(e.getMessage());

            try {
                logger.debug("Do rollback...");
                conn.rollback();
                logger.debug("Do rollback... DONE");
            } catch (SQLException e1) {
                logger.error(e1.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());

            try {
                logger.debug("Do rollback...");
                conn.rollback();
                logger.debug("Do rollback... DONE");
            } catch (SQLException e1) {
                logger.error(e1.getMessage());
            }

        } finally {

            // close result set if needed
            SQLHelper.closeResultSet(resultSet);

            // close prepared statement if needed
            SQLHelper.closePreparedStatement(preparedStatement);

            // set original value of autocommit
            try {
                if (conn != null) {
                    logger.debug("Set original value of autocommit...");
                    conn.setAutoCommit(previousValueOfAutocommit);
                    logger.debug("Set original value of autocommit... DONE");
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }

        }
    }
}
