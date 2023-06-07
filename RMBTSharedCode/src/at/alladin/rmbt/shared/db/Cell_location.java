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

public class Cell_location {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(Cell_location.class);

    private static final String SQL_INSERT_CELL_INFO =
            "INSERT INTO cell_location(test_id, time, location_id, area_code, primary_scrambling_code, time_ns) "
                    + "VALUES( ?, ?, ?, ?, ?,?)";

    private long uid;
    private long test_id;
    private Timestamp time;
    private int location_id;
    private int area_code;
    private int primary_scrambling_code;
    private long time_ns;

    private Calendar timeZone = null;

    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;

    public Cell_location(final Connection conn) {
        reset();
        this.conn = conn;
    }

    public Cell_location(final Connection conn, final long uid, final long test_id, final Timestamp time,
                         final int location_id, final int area_code, final int primary_scrambling_code, final String timeZoneId, final long time_ns) {

        reset();

        this.conn = conn;

        this.uid = uid;
        this.test_id = test_id;
        this.time = time;
        this.time_ns = time_ns;
        this.location_id = location_id;
        this.area_code = area_code;
        this.primary_scrambling_code = primary_scrambling_code;

        timeZone = Helperfunctions.getTimeWithTimeZone(timeZoneId);
    }

    public void reset() {

        uid = 0;
        test_id = 0;
        time = null;
        location_id = 0;
        area_code = 0;
        primary_scrambling_code = 0;

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

    public void storeLocation() {
        PreparedStatement st;
        try {
            st = conn.prepareStatement(
                    SQL_INSERT_CELL_INFO, Statement.RETURN_GENERATED_KEYS);
            
            /*
             * Timestamp geotstamp = java.sql.Timestamp.valueOf(new Timestamp(
             * this.time).toString());
             */

            st.setLong(1, test_id);
            st.setTimestamp(2, time, timeZone);
            st.setInt(3, location_id);
            st.setInt(4, area_code);
            st.setInt(5, primary_scrambling_code);
            st.setLong(6, time_ns);

            logger.debug(st.toString());
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_CELLLOCATION");
            else {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    // Retrieve the auto generated key(s).
                    uid = rs.getInt(1);
            }

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            setError("ERROR_DB_STORE_CELLLOCATION_SQL");
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

    public long getTime_ns() {
        return time_ns;
    }

    public void setTime_ns(long time_ns) {
        this.time_ns = time_ns;
    }

    public int getLocation_id() {
        return location_id;
    }

    public int getArea_code() {
        return area_code;
    }

    public int getPrimary_scrambling_code() {
        return primary_scrambling_code;
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

    public void setLocation_id(final int location_id) {
        this.location_id = location_id;
    }

    public void setArea_code(final int area_code) {
        this.area_code = area_code;
    }

    public void setPrimary_scrambling_code(final int primary_scrambling_code) {
        this.primary_scrambling_code = primary_scrambling_code;
    }

}
