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
package at.alladin.rmbt.shared.db.dao;

import at.alladin.rmbt.shared.db.QoSTestResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lb
 */
public class QoSTestResultDao implements CrudPrimaryKeyDao<QoSTestResult, Long> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(QoSTestResultDao.class);

    private static final String SQL_SELECT_TESTS_BY_UID = "SELECT nntr.uid, test_uid, success_count, failure_count, nnto.test, result AS result, "
            + " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
//            + " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE test_uid = ? AND nntr.deleted = 'FALSE' and nntr.implausible = 'FALSE'";
            + " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE test_uid = ? AND nntr.deleted = 'FALSE'";

    private static final String SQL_SELECT_BY_UID = "SELECT nntr.uid, test_uid, nnto.test, success_count, failure_count, result AS result, "
            + " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
//            + " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE nntr.uid = ? AND nntr.deleted = 'FALSE' and nntr.implausible = 'FALSE'";
            + " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid WHERE nntr.uid = ? AND nntr.deleted = 'FALSE'";

    private static final String SQL_SELECT_ALL = "SELECT nntr.uid, test_uid, nnto.test, success_count, failure_count, result AS result, "
            + " nnto.results as results, qos_test_uid, nnto.test_desc, nnto.test_summary FROM qos_test_result nntr "
            + " JOIN qos_test_objective nnto ON nntr.qos_test_uid = nnto.uid";

    private static final String SQL_INSERT_QoS_TEST_RESULT =
            "INSERT INTO qos_test_result (test_uid, result, qos_test_uid, success_count, failure_count) VALUES (?,?::json,?,?,?)";

    private static final String SQL_UPDATE_QoS_TEST_RESULT_JSON =
            "UPDATE qos_test_result SET test_uid = ?, result = ?::json, qos_test_uid = ?, success_count = ?, failure_count = ? WHERE uid = ?";

    private static final String SQL_UPDATE_QoS_TEST_RESULT =
            "UPDATE qos_test_result SET success_count = ?, failure_count = ? WHERE uid = ?";

    private final Connection conn;

    /**
     * @param conn
     */
    public QoSTestResultDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * @param testUid
     * @return
     * @throws SQLException
     */
    public List<QoSTestResult> getByTestUid(Long testUid) throws SQLException {
        List<QoSTestResult> resultList = new ArrayList<>();

        try (PreparedStatement psGetAll = conn.prepareStatement(SQL_SELECT_TESTS_BY_UID)) {
            psGetAll.setLong(1, testUid);

            logger.debug(psGetAll.toString());
            if (psGetAll.execute()) {
                try (ResultSet rs = psGetAll.getResultSet()) {
                    while (rs.next()) {
                        resultList.add(instantiateItem(rs));
                    }
                }
            } else {
                throw new SQLException("item not found");
            }

            return resultList;
        }
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getById(java.lang.Object)
     */
    @Override
    public QoSTestResult getById(Long id) throws SQLException {
        try (PreparedStatement psGetById = conn.prepareStatement(SQL_SELECT_BY_UID)) {
            psGetById.setLong(1, id);

            logger.debug(psGetById.toString());
            if (psGetById.execute()) {
                try (ResultSet rs = psGetById.getResultSet()) {
                    if (rs.next()) {
                        QoSTestResult nntr = instantiateItem(rs);
                        return nntr;
                    } else {
                        throw new SQLException("empty result set");
                    }
                }
            } else {
                throw new SQLException("no result set");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getAll()
     */
    @Override
    public List<QoSTestResult> getAll() throws SQLException {
        List<QoSTestResult> resultList = new ArrayList<>();

        try (PreparedStatement psGetAll = conn.prepareStatement(SQL_SELECT_ALL)) {
            logger.debug(psGetAll.toString());
            if (psGetAll.execute()) {
                try (ResultSet rs = psGetAll.getResultSet()) {
                    while (rs.next()) {
                        resultList.add(instantiateItem(rs));
                    }
                }
            } else {
                throw new SQLException("item not found");
            }

            return resultList;
        }
    }


    @Override
    public int update(QoSTestResult entity) throws SQLException {
        return save(entity);
    }

    /**
     * @param result
     * @throws SQLException
     */
    public int save(QoSTestResult result) throws SQLException {
        String sql;

        PreparedStatement ps = null;

        if (result.getUid() == null) {
            ps = conn.prepareStatement(SQL_INSERT_QoS_TEST_RESULT);
            ps.setLong(1, result.getTestUid());
            ps.setObject(2, result.getResults());
            ps.setLong(3, result.getQoSTestObjectiveId());
            ps.setInt(4, result.getSuccessCounter());
            ps.setInt(5, result.getFailureCounter());
        } else {
            ps = conn.prepareStatement(SQL_UPDATE_QoS_TEST_RESULT_JSON);
            ps.setLong(1, result.getTestUid());
            ps.setObject(2, result.getResults());
            ps.setLong(3, result.getQoSTestObjectiveId());
            ps.setInt(4, result.getSuccessCounter());
            ps.setInt(5, result.getFailureCounter());
            ps.setLong(6, result.getUid());
        }

        logger.debug(ps.toString());
        return ps.executeUpdate();
    }

    /**
     * @param resultCollection
     * @throws SQLException
     */
    public void saveAll(Collection<QoSTestResult> resultCollection) throws SQLException {
        for (QoSTestResult result : resultCollection) {
            save(result);
        }
    }

    /**
     * @return
     * @throws SQLException
     */
    public PreparedStatement getUpdateCounterPreparedStatement() throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_QoS_TEST_RESULT);
        return ps;
    }

    /**
     * @param result
     * @return
     * @throws SQLException
     */
    public int updateCounter(QoSTestResult result) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_QoS_TEST_RESULT);
        return updateCounter(result, ps);
    }

    /**
     * @param result
     * @param ps
     * @throws SQLException
     */
    public int updateCounter(QoSTestResult result, PreparedStatement ps) throws SQLException {
        ps.setInt(1, result.getSuccessCounter());
        ps.setInt(2, result.getFailureCounter());
        ps.setLong(3, result.getUid());
        logger.debug(ps.toString());
        return ps.executeUpdate();
    }

    /**
     * @param resultCollection
     * @throws SQLException
     */
    public void updateCounterAll(Collection<QoSTestResult> resultCollection) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_QoS_TEST_RESULT);

        for (QoSTestResult result : resultCollection) {
            updateCounter(result, ps);
        }
    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    private static QoSTestResult instantiateItem(ResultSet rs) throws SQLException {
        QoSTestResult result = new QoSTestResult();

        result.setUid(rs.getLong("uid"));
        result.setTestType(rs.getString("test"));
        result.setResults(rs.getString("result"));
        result.setTestUid(rs.getLong("test_uid"));
        result.setQoSTestObjectiveId(rs.getLong("qos_test_uid"));
        result.setTestDescription(rs.getString("test_desc"));
        result.setTestSummary(rs.getString("test_summary"));
        result.setSuccessCounter(rs.getInt("success_count"));
        result.setFailureCounter(rs.getInt("failure_count"));

        final String results = rs.getString("results");
        try {
            result.setExpectedResults(results != null ? new JSONArray(results) : null);
        } catch (JSONException e) {
            result.setExpectedResults(null);
            logger.error(e.getMessage());
        }

        return result;
    }

    @Override
    public int delete(QoSTestResult entity) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
