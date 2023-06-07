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

import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.db.fields.DoubleField;
import at.alladin.rmbt.shared.db.fields.Field;
import at.alladin.rmbt.shared.db.fields.LongField;
import at.alladin.rmbt.shared.db.fields.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class TestNdt extends Table {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestNdt.class);

    private final static String SELECT = "SELECT" + " *" + " FROM test_ndt tn";
    private final static String SQL_INSERT_TEST_NDT = "INSERT INTO test_ndt " + "(%s) VALUES (%s)";

    private final static ThreadLocal<Field[]> PER_THREAD_FIELDS = new ThreadLocal<Field[]>() {
        protected Field[] initialValue() {
            return new Field[]{
                    new LongField("test_id", null),
                    new DoubleField("s2cspd", "s2cspd"),
                    new DoubleField("c2sspd", "c2sspd"),
                    new DoubleField("avgrtt", "avgrtt"),
                    new StringField("main", "main"),
                    new StringField("stat", "stat"),
                    new StringField("diag", "diag"),
                    new LongField("time_ns", "time_ns"),
                    new LongField("time_end_ns", "time_end_ns")
            };
        }
    };

    public TestNdt(final Connection conn) {
        super(PER_THREAD_FIELDS.get(), conn);
    }

    public void storeTest() {
        try {
            final StringBuilder keys = new StringBuilder();
            final StringBuilder values = new StringBuilder();
            for (final Field field : fields)
                if (!field.isReadOnly()) {
                    field.appendDbKey(keys);
                    field.appendDbValue(values);
                }

            PreparedStatement st;
            st = conn.prepareStatement(String.format(SQL_INSERT_TEST_NDT, keys, values),
                    Statement.RETURN_GENERATED_KEYS);

            int idx = 1;
            for (final Field field : fields)
                if (!field.isReadOnly())
                    field.getField(st, idx++);

            logger.debug(st.toString());
            final int affectedRows = st.executeUpdate();
            if (affectedRows == 0)
                setError("ERROR_DB_STORE_TEST");
            else {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    uid = rs.getLong(1);

                // close result set
                SQLHelper.closeResultSet(rs);
            }
        } catch (final SQLException e) {
            setError("ERROR_DB_STORE_TEST_SQL");
            logger.error(e.getMessage());
        }
    }

    public boolean loadByTestId(final long testId) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT + " WHERE tn.test_id = ?");
            st.setLong(1, testId);
            rs = st.executeQuery();

            if (rs.next())
                setValuesFromResult(rs);
            else
                return false;

            return true;
        } catch (final SQLException e) {
            logger.error(e.getMessage());
        } finally {

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        }
        return false;
    }
}
