/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.shared;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SpeedGraph {

    private static final String SQL_TEST_SUM_SPEED =
            "SELECT upload, sumbytes, time as ms FROM test_sum_speed "
                    + "WHERE test_id = ? and (sumbytes IS NOT NULL) ORDER BY upload, time ASC";

    private ArrayList<SpeedGraphItem> upload = new ArrayList<>();
    private ArrayList<SpeedGraphItem> download = new ArrayList<>();

    /**
     * Load download and upload speed details
     *
     * @param testUID the test uid
     * @param threads the max number of threads used in the test
     * @throws SQLException
     */

    // tj
    public SpeedGraph(long testUID, int threads, java.sql.Connection conn) throws SQLException {
        PreparedStatement psSpeed = conn.prepareStatement(SQL_TEST_SUM_SPEED);
        psSpeed.setLong(1, testUID);
        ResultSet rsSpeed = psSpeed.executeQuery();
        long ms = 0;
        long bytesCum = 0;
        while (rsSpeed.next()) {
            ms = rsSpeed.getLong("ms");
            bytesCum = rsSpeed.getLong("sumbytes");
            ArrayList<SpeedGraphItem> array = (rsSpeed.getBoolean("upload")) ? this.upload : this.download;
            SpeedGraphItem obj = new SpeedGraphItem(ms, bytesCum);
            array.add(obj);
        }

        // close result set
        SQLHelper.closeResultSet(rsSpeed);

        // close prepared statement
        SQLHelper.closePreparedStatement(psSpeed);
    }

    /*
    public SpeedGraph(long testUID, int threads, java.sql.Connection conn) throws SQLException {
        PreparedStatement psSpeed = conn
                .prepareStatement("SELECT upload, thread, bytes, (time::float /1000/1000) as time FROM test_speed WHERE test_id = ? ORDER BY upload, time ASC");
        psSpeed.setLong(1, testUID);
        // Prepare arrays (bytes cumulated per thread)
        long bytes[] = new long[threads];
        ResultSet rsSpeed = psSpeed.executeQuery();
        long bytesCum = 0;
        boolean upload = false;
        double lastMs = -1;
        SpeedGraphItem lastObj = null; // the last object => if there are more
                                    // than one entries for one timestamp
        while (rsSpeed.next()) {
            int thread = rsSpeed.getInt("thread");
            double ms = rsSpeed.getDouble("time");

            // if its the first time a upload => clear array
            if (!upload && rsSpeed.getBoolean("upload")) {
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = 0;
                }
                bytesCum = 0;
                upload = true;
            }

            // bytesCum = bytesCum - old + new
            bytesCum = bytesCum - bytes[thread];
            bytes[thread] = rsSpeed.getLong("bytes");
            bytesCum = bytesCum + bytes[thread];

            // put the object in the json-response
            ArrayList<SpeedGraphItem> array = (rsSpeed.getBoolean("upload")) ? this.upload
                    : this.download;

            // if it is a new timestamp => make new array
            if (lastMs != ms) {
                SpeedGraphItem obj = new SpeedGraphItem((long)ms,bytesCum);
                array.add(obj);
                lastObj = obj;
                lastMs = ms;
            } else {
                // if it is the same time => update the previous timestamp
                lastObj.setBytesTotal(bytesCum);
            }
        }

        rsSpeed.close();
        psSpeed.close();
    }
    */
    public ArrayList<SpeedGraphItem> getUpload() {
        return this.upload;
    }

    public ArrayList<SpeedGraphItem> getDownload() {
        return this.download;
    }

    public class SpeedGraphItem {
        private long timeElapsed;
        private long bytesTotal;

        public SpeedGraphItem(long timeElapsed, long bytesTotal) {
            this.timeElapsed = timeElapsed;
            this.bytesTotal = bytesTotal;
        }

        /**
         * @return The time elapsed since the begin of the test
         */
        public long getTimeElapsed() {
            return this.timeElapsed;
        }

        /**
         * @return The total bytes transmitted in all threads since the begin of the test
         */
        public long getBytesTotal() {
            return this.bytesTotal;
        }

        public void setBytesTotal(long bytesTotal) {
            this.bytesTotal = bytesTotal;
        }
    }

}