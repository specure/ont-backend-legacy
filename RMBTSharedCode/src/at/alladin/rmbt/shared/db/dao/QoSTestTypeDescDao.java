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

import at.alladin.rmbt.shared.db.QoSTestResult.TestType;
import at.alladin.rmbt.shared.db.QoSTestTypeDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QoSTestTypeDescDao implements PrimaryKeyDao<QoSTestTypeDesc, Long> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(QoSTestTypeDescDao.class);

    private static final String SQL_SELECT_QoS_TEST_DESC =
            "SELECT nnttd.uid AS uid, test, nntd.\"value\" "
                    + " FROM qos_test_type_desc AS nnttd JOIN qos_test_desc nntd ON nnttd.test_desc = nntd.desc_key WHERE uid = ?";

    private static final String SQL_SELECT_QoS_TEST_DESC_ALL =
            "SELECT nnttd.uid AS uid, test, nntd.\"value\", nntd.lang, nntd2.\"value\" AS value_name, nntd2.lang AS name_lang "
                    + " FROM qos_test_type_desc AS nnttd "
                    + " JOIN qos_test_desc nntd ON nnttd.test_desc = nntd.desc_key "
                    + " JOIN qos_test_desc nntd2 ON nnttd.test_name = nntd2.desc_key WHERE nntd.lang = ("
                    + " CASE WHEN EXISTS(SELECT 1 FROM qos_test_desc WHERE desc_key = nntd.desc_key AND lang = ?) "
                    + "     THEN ? ELSE 'en' END) AND nntd2.lang = ("
                    + " CASE WHEN EXISTS(SELECT 1 FROM qos_test_desc WHERE desc_key = nntd2.desc_key AND lang = ?) "
                    + "     THEN ? ELSE 'en' END)";

    private static final String SQL_SELECT_QoS_DESC = "SELECT nnttd.uid AS uid, test, nntd.\"value\", nntd.lang, nntd2.\"value\" AS value_name, nntd2.lang AS name_lang "
            + " FROM qos_test_type_desc AS nnttd "
            + " JOIN qos_test_desc nntd ON nnttd.test_desc = nntd.desc_key "
            + " JOIN qos_test_desc nntd2 ON nnttd.test_name = nntd2.desc_key"
            + " WHERE nntd.lang = ("
            + " CASE WHEN EXISTS(SELECT 1 FROM qos_test_desc WHERE desc_key = nntd.desc_key AND lang = ?) "
            + "     THEN ? ELSE 'en' END)"
            + " AND nntd2.lang = ("
            + " CASE WHEN EXISTS(SELECT 1 FROM qos_test_desc WHERE desc_key = nntd2.desc_key AND lang = ?) "
            + "     THEN ? ELSE 'en' END)";

    /**
     *
     */
    private final Connection conn;

    /**
     *
     */
    private Locale locale;

    /**
     * @param conn
     */
    public QoSTestTypeDescDao(final Connection conn, Locale locale) {
        this.conn = conn;
        this.locale = locale;
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.db.dao.PrimaryKeyDao#getById(java.lang.Object)
     */
    @Override
    public QoSTestTypeDesc getById(Long id) throws SQLException {
        try (PreparedStatement psGetById = conn.prepareStatement(SQL_SELECT_QoS_TEST_DESC)) {
            psGetById.setLong(1, id);

            logger.debug(psGetById.toString());
            if (psGetById.execute()) {
                try (ResultSet rs = psGetById.getResultSet()) {
                    if (rs.next()) {
                        QoSTestTypeDesc nntd = instantiateItem(rs);
                        return nntd;
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
    public List<QoSTestTypeDesc> getAll() throws SQLException {
        List<QoSTestTypeDesc> resultList = new ArrayList<>();
        try (PreparedStatement psGetAll = conn.prepareStatement(SQL_SELECT_QoS_TEST_DESC_ALL)) {

            psGetAll.setString(1, locale.getLanguage());
            psGetAll.setString(2, locale.getLanguage());
            psGetAll.setString(3, locale.getLanguage());
            psGetAll.setString(4, locale.getLanguage());

            logger.debug(psGetAll.toString());
            if (psGetAll.execute()) {
                try (ResultSet rs = psGetAll.getResultSet()) {
                    while (rs.next()) {
                        resultList.add(instantiateItem(rs));
                    }
                }
                return resultList;
            } else {
                throw new SQLException("no result set");
            }
        }
    }

    /**
     * @return
     * @throws SQLException
     */
    public HashMap<TestType, QoSTestTypeDesc> getAllToMap() throws SQLException {
        HashMap<TestType, QoSTestTypeDesc> resultMap = new HashMap<>();
        try (PreparedStatement psGetAll = conn.prepareStatement(SQL_SELECT_QoS_DESC)) {
            psGetAll.setString(1, locale.getLanguage());
            psGetAll.setString(2, locale.getLanguage());
            psGetAll.setString(3, locale.getLanguage());
            psGetAll.setString(4, locale.getLanguage());

            logger.debug(psGetAll.toString());
            if (psGetAll.execute()) {
                try (ResultSet rs = psGetAll.getResultSet()) {
                    while (rs.next()) {
                        QoSTestTypeDesc item = instantiateItem(rs);
                        resultMap.put(item.getTestType(), item);
                    }
                    return resultMap;
                }
            } else {
                throw new SQLException("no result set");
            }
        }
    }


    /**
     * @param rs
     * @return
     * @throws SQLException
     */
    private static QoSTestTypeDesc instantiateItem(ResultSet rs) throws SQLException {
        QoSTestTypeDesc result = new QoSTestTypeDesc();

        result.setUid(rs.getLong("uid"));
        result.setDescription(rs.getString("value"));
        result.setName(rs.getString("value_name"));
        try {
            result.setTestType(TestType.valueOf(rs.getString("test").toUpperCase(Locale.US)));
        } catch (Exception e) {
            result.setTestType(null);
        }

        return result;
    }

}
