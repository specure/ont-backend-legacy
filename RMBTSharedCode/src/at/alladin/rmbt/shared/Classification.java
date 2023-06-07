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
package at.alladin.rmbt.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public final class Classification {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(Classification.class);

    private static final String SQL_SETTINGS = "SELECT value FROM settings WHERE key = ?";

    public static Classification getInstance() {
        return instance;
    }

    private static Classification instance;

    public static void initInstance(Connection conn) {
        instance = new Classification(conn);
    }

    private Classification(Connection conn) {
        int[] uploadValues = null;
        int[] downloadValues = null;
        int[] jitterValues = null;
        int[] packetLossValues = null;
        try (PreparedStatement ps = conn.prepareStatement(SQL_SETTINGS)) {
            uploadValues = getIntValues(ps, "threshold_upload", 2);
            downloadValues = getIntValues(ps, "threshold_download", 2);
            jitterValues = getIntValues(ps, "threshold_jitter", 2);
            packetLossValues = getIntValues(ps, "threshold_packet_loss", 2);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (uploadValues == null)
            uploadValues = new int[]{1000, 500}; // default
        THRESHOLD_UPLOAD = uploadValues;
        THRESHOLD_UPLOAD_CAPTIONS = getCaptions(uploadValues);

        if (downloadValues == null)
            downloadValues = new int[]{2000, 1000}; // default
        THRESHOLD_DOWNLOAD = downloadValues;
        THRESHOLD_DOWNLOAD_CAPTIONS = getCaptions(downloadValues);

        THRESHOLD_JITTER = jitterValues;
        THRESHOLD_JITTER_CAPTIONS = getCaptions(jitterValues);

        THRESHOLD_PACKET_LOSS = packetLossValues;
        THRESHOLD_PACKET_LOSS_CAPTIONS = getCaptions(packetLossValues);
    }

    private static String[] getCaptions(int[] values) {
        final String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++)
            result[i] = String.format(Locale.US, "%.1f", ((double) values[i]) / 1000);
        return result;
    }

    private static int[] getIntValues(PreparedStatement ps, String key, int expectCount)
            throws SQLException, NumberFormatException, IllegalArgumentException {
        ps.setString(1, key);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next())
                return null;
            final String value = rs.getString("value");
            if (value == null)
                return null;
            final String[] parts = value.split(";");
            if (parts.length != expectCount)
                throw new IllegalArgumentException(String.format(Locale.US,
                        "unexpected number of parameters (expected %d): \"%s\"", expectCount, value));
            final int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++)
                result[i] = Integer.parseInt(parts[i]);
            return result;
        }
    }

    public final int[] THRESHOLD_UPLOAD;
    public final String[] THRESHOLD_UPLOAD_CAPTIONS;

    public final int[] THRESHOLD_DOWNLOAD;
    public final String[] THRESHOLD_DOWNLOAD_CAPTIONS;

    public final int[] THRESHOLD_PING = {25000000, 75000000};
    public final String[] THRESHOLD_PING_CAPTIONS = {"25", "75"};

    // RSSI limits used for 2G,3G (and 4G when RSSI is used)
    // only odd values are reported by 2G/3G
    public final int[] THRESHOLD_SIGNAL_MOBILE = {-85, -101}; // -85 is still green, -101 is still
    // yellow
    public final String[] THRESHOLD_SIGNAL_MOBILE_CAPTIONS = {"-85", "-101"};

    // RSRP limit used for 4G
    public final int[] THRESHOLD_SIGNAL_RSRP = {-95, -111};
    public final String[] THRESHOLD_SIGNAL_RSRP_CAPTIONS = {"-95", "-111"};

    // RSRP limit used for 5G
    public final int[] THRESHOLD_SIGNAL_SSRSRP = {-80, -110};
    public final String[] THRESHOLD_SIGNAL_SSRSRP_CAPTIONS = {"-80", "-110"};

    // RSSI limits used for Wifi
    public final int[] THRESHOLD_SIGNAL_WIFI = {-61, -76};
    public final String[] THRESHOLD_SIGNAL_WIFI_CAPTIONS = {"-61", "-76"};

    // Jitter
    public final int[] THRESHOLD_JITTER;
    public final String[] THRESHOLD_JITTER_CAPTIONS;

    // Packet loss
    public final int[] THRESHOLD_PACKET_LOSS;
    public final String[] THRESHOLD_PACKET_LOSS_CAPTIONS;

    public static int classifyJitter(int[] threshold, double value) {
        final boolean inverse = threshold[0] < threshold[1];

        if (!inverse) {
            if (value >= threshold[0])
                return 3; // GREEN
            else if (value >= threshold[1])
                return 2; // YELLOW
            else
                return 1; // RED
        } else if (value <= threshold[0])
            return 3;
        else if (value <= threshold[1])
            return 2;
        else
            return 1;
    }


    public static int classifyPacketLoss(final int[] threshold, final double value) {
        final boolean inverse = threshold[0] < threshold[1];

        if (inverse) {
            if (value == 0D) {
                return 3; // GREEN
            } else if (value > 0D && value <= threshold[0]) {
                return 2; // YELLOW
            } else {
                return 1; // RED
            }
        } else if (value == 0D) {
            return 3;
        } else if (value > 0D && value <= threshold[1]) {
            return 2;
        } else {
            return 1;
        }
    }

    public static int classify(final int[] threshold, final double value) {
        final boolean inverse = threshold[0] < threshold[1];

        if (!inverse) {
            if (value >= threshold[0])
                return 3; // GREEN
            else if (value >= threshold[1])
                return 2; // YELLOW
            else
                return 1; // RED
        } else if (value < threshold[0])
            return 3;// GREEN
        else if (value < threshold[1])
            return 2;// YELLOW
        else
            return 1;// RED
    }


}
