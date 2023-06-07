/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
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
package at.alladin.rmbt.shared.db.dao;

import at.alladin.rmbt.shared.db.TestStat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestStatDao implements CrudPrimaryKeyDao<TestStat, Long> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestStatDao.class);

    private static final String SQL_SELECT_TEST_STAT_BY_UID =
            "SELECT test_uid, cpu_usage, mem_usage FROM test_stat WHERE test_uid = ?";

    private static final String SQL_INSERT_TEST_STAT =
            "INSERT INTO test_stat (test_uid, cpu_usage, mem_usage) VALUES (?,?::json,?::json)";

    private static final String SQL_UPDATE_TEST_STAT =
            "UPDATE test_stat SET cpu_usage = ?::json, mem_usage = ?::json, WHERE test_uid = ?";

    private final Connection conn;

    /**
     * @param conn
     */
    public TestStatDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public TestStat getById(Long id) throws SQLException {
        try (PreparedStatement psGetById = conn.prepareStatement(SQL_SELECT_TEST_STAT_BY_UID)) {
            psGetById.setLong(1, id);

            logger.debug(psGetById.toString());
            if (psGetById.execute()) {
                try (ResultSet rs = psGetById.getResultSet()) {
                    if (rs.next()) {
                        final TestStat ts = instantiateItem(rs);
                        return ts;
                    }
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                    return null;
                }
            }

            return null;
        }
    }

    @Override
    public List<TestStat> getAll() throws SQLException {
        final List<TestStat> resultList = new ArrayList<>();
        try (PreparedStatement psGetAll = conn.prepareStatement(SQL_SELECT_TEST_STAT_BY_UID)) {

            logger.debug(psGetAll.toString());
            if (psGetAll.execute()) {
                try (ResultSet rs = psGetAll.getResultSet()) {
                    while (rs.next()) {
                        resultList.add(instantiateItem(rs));
                    }

                    return resultList;
                } catch (JSONException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        return null;
    }

    @Override
    public int delete(TestStat entity) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int save(TestStat result) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TEST_STAT);
        ps.setLong(1, result.getTestUid());
        ps.setString(2, result.getCpuUsage().toString());
        ps.setString(3, result.getMemUsage().toString());
        logger.debug(ps.toString());
        return ps.executeUpdate();
    }

    @Override
    public int update(TestStat entity) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_TEST_STAT);
        ps.setString(1, entity.getCpuUsage().toString());
        ps.setString(2, entity.getMemUsage().toString());
        ps.setLong(3, entity.getTestUid());
        logger.debug(ps.toString());
        return ps.executeUpdate();
    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     * @throws JSONException
     */
    private static TestStat instantiateItem(ResultSet rs) throws SQLException, JSONException {
        TestStat result = new TestStat();
        result.setTestUid(rs.getLong("test_uid"));
        result.setCpuUsage(new JSONObject(rs.getString("cpu_usage")));
        result.setMemUsage(new JSONObject(rs.getString("mem_usage")));
        return result;
    }
}
