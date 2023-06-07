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
package at.alladin.rmbt.shared.db;

import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.SQLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Calendar;

public class Signal {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(Signal.class);

    private static final String SQL_INSERT_SIGNAL = "INSERT INTO signal("
            + "test_id, time, network_type_id, signal_strength, gsm_bit_error_rate, wifi_link_speed, wifi_rssi, "
            + "lte_rsrp, lte_rsrq, lte_rssnr, lte_cqi, time_ns, ss_rsrp) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    final static int UNKNOWN = Integer.MIN_VALUE;

    private long uid;
    private long test_id;
    private Timestamp time;
    private int network_type_id;
    private int signal_strength;    // RSSI value, used in GSM, UMTS and Wifi (sometimes in LTE)
    private int gsm_bit_error_rate;
    private int wifi_link_speed;
    private int wifi_rssi;
    private int lte_rsrp;           // signal strength value as RSRP, used in LTE
    private int lte_rsrq;           // signal quality RSRQ, used in LTE
    private int lte_rssnr;
    private int lte_cqi;
    private long time_ns;            // relative ts in ns
    private int ss_rsrp;            // 5G signal strength

    private Calendar timeZone = null;

    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;

    public Signal(final Connection conn) {
        reset();
        this.conn = conn;
    }

    public Signal(final Connection conn, final long uid, final long test_id, final Timestamp time,
                  final int network_type_id, final int signal_strength, final int gsm_bit_error_rate,
                  final int wifi_link_speed, final int wifi_rssi,
                  final int lte_rsrp, final int lte_rsrq, final int lte_rssnr, final int lte_cqi,
                  final String timeZoneId, final long time_ns, final int ss_rsrp) {

        reset();

        this.conn = conn;

        this.uid = uid;
        this.test_id = test_id;
        this.time = time;
        this.network_type_id = network_type_id;
        this.signal_strength = signal_strength;
        this.gsm_bit_error_rate = gsm_bit_error_rate;
        this.wifi_link_speed = wifi_link_speed;
        this.wifi_rssi = wifi_rssi;
        this.lte_rsrp = lte_rsrp;
        this.lte_rsrq = lte_rsrp;
        this.lte_rssnr = lte_rsrp;
        this.lte_cqi = lte_cqi;
        this.time_ns = time_ns;
        this.ss_rsrp = ss_rsrp;

        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }

    public void reset() {

        uid = 0;
        test_id = UNKNOWN;
        time = null;
        network_type_id = UNKNOWN;
        signal_strength = UNKNOWN;
        gsm_bit_error_rate = UNKNOWN;
        wifi_link_speed = UNKNOWN;
        wifi_rssi = UNKNOWN;
        lte_rsrp = UNKNOWN;
        lte_rsrq = UNKNOWN;
        lte_rssnr = UNKNOWN;
        lte_cqi = UNKNOWN;
        time_ns = UNKNOWN;
        ss_rsrp = UNKNOWN;

        timeZone = null;

        resetError();
    }

    private void resetError() {
        error = false;
        errorLabel = "";
    }

    private void setError(final String errorLabel) {
        error = true;
        this.errorLabel = errorLabel;
    }

    public void storeSignal() {
        PreparedStatement st;
        try {
            st = conn.prepareStatement(
                    SQL_INSERT_SIGNAL, Statement.RETURN_GENERATED_KEYS);

            if (test_id == UNKNOWN)
                st.setNull(1, Types.BIGINT);
            else
                st.setLong(1, test_id);

            if (time == null)
                st.setNull(2, Types.TIMESTAMP);
            else
                st.setTimestamp(2, time, timeZone);

            if (network_type_id == UNKNOWN)
                st.setNull(3, Types.INTEGER);
            else
                st.setInt(3, network_type_id);

            if (signal_strength == UNKNOWN)
                st.setNull(4, Types.INTEGER);
            else
                st.setInt(4, signal_strength);

            if (gsm_bit_error_rate == UNKNOWN)
                st.setNull(5, Types.INTEGER);
            else
                st.setInt(5, gsm_bit_error_rate);

            if (wifi_link_speed == UNKNOWN)
                st.setNull(6, Types.INTEGER);
            else
                st.setInt(6, wifi_link_speed);

            if (wifi_rssi == UNKNOWN)
                st.setNull(7, Types.INTEGER);
            else
                st.setInt(7, wifi_rssi);

            if (lte_rsrp == UNKNOWN)
                st.setNull(8, Types.INTEGER);
            else
                st.setInt(8, lte_rsrp);

            if (lte_rsrq == UNKNOWN)
                st.setNull(9, Types.INTEGER);
            else
                st.setInt(9, lte_rsrq);

            if (lte_rssnr == UNKNOWN)
                st.setNull(10, Types.INTEGER);
            else
                st.setInt(10, lte_rssnr);

            if (lte_cqi == UNKNOWN)
                st.setNull(11, Types.INTEGER);
            else
                st.setInt(11, lte_cqi);

            if (time_ns == UNKNOWN)
                st.setNull(12, Types.BIGINT);
            else
                st.setLong(12, time_ns);

            if (ss_rsrp == UNKNOWN)
                st.setNull(13, Types.INTEGER);
            else
                st.setInt(13, ss_rsrp);

            logger.debug(st.toString());
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_SIGNAL");
            else {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    // Retrieve the auto generated key(s).
                    uid = rs.getInt(1);
            }

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            setError("ERROR_DB_STORE_SIGNAL_SQL");
            logger.error(e.getMessage());
        }
    }

    public boolean hasError() {
        return error;
    }

    public String getError() {
        return errorLabel;
    }

    public long getUid() {
        return uid;
    }

    public long getTest_id() {
        return test_id;
    }

    public Timestamp getTime() {
        return time;
    }

    public int getNetwork_type_id() {
        return network_type_id;
    }

    public int getSignal_strength() {
        return signal_strength;
    }

    public int getGsm_bit_error_rate() {
        return gsm_bit_error_rate;
    }

    public int getWifi_link_speed() {
        return wifi_link_speed;
    }

    public int getWifi_rssi() {
        return wifi_rssi;
    }

    public void setUid(final long uid) {
        this.uid = uid;
    }

    public void setTest_id(final long test_id) {
        this.test_id = test_id;
    }

    public void setTime(final Timestamp time, final String timeZoneId) {
        this.time = time;
        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }

    public void setNetwork_type_id(final int network_type_id) {
        this.network_type_id = network_type_id;
    }

    public void setSignal_strength(final int signal_strength) {
        this.signal_strength = signal_strength;
    }

    public void setGsm_bit_error_rate(final int gsm_bit_error_rate) {
        this.gsm_bit_error_rate = gsm_bit_error_rate;
    }

    public void setWifi_link_speed(final int wifi_link_speed) {
        this.wifi_link_speed = wifi_link_speed;
    }

    public void setWifi_rssi(final int wifi_rssi) {
        this.wifi_rssi = wifi_rssi;
    }

    public int getLte_rsrp() {
        return lte_rsrp;
    }

    public void setLte_rsrp(int lte_rsrp) {
        this.lte_rsrp = lte_rsrp;
    }

    public int getLte_rsrq() {
        return lte_rsrq;
    }

    public void setLte_rsrq(int lte_rsrq) {
        this.lte_rsrq = lte_rsrq;
    }

    public int getLte_rssnr() {
        return lte_rssnr;
    }

    public void setLte_rssnr(int lte_rssnr) {
        this.lte_rssnr = lte_rssnr;
    }

    public int getLte_cqi() {
        return lte_cqi;
    }

    public void setLte_cqi(int lte_cqi) {
        this.lte_cqi = lte_cqi;
    }

    public long getTime_ns() {
        return time_ns;
    }

    public void setTime_ns(long time_ns) {
        this.time_ns = time_ns;
    }

    public int getSs_rsrp() {
        return ss_rsrp;
    }

    public void setSs_rsrp(int ss_rsrp) {
        this.ss_rsrp = ss_rsrp;
    }
}
