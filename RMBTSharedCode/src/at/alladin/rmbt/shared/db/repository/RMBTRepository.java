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
package at.alladin.rmbt.shared.db.repository;

import at.alladin.rmbt.shared.db.annotation.DatabasePrimaryKey;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class RMBTRepository<T> extends AbstractRepository {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(RMBTRepository.class);

    public static class Page {
        Integer limit;

        Long offset;

        public Page(final Integer limit, final Long offset) {
            this.limit = limit;
            this.offset = offset;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Long getOffset() {
            return offset;
        }

        public void setOffset(Long offset) {
            this.offset = offset;
        }
    }

    public static class OrderBy {
        public enum Order {
            ASC,
            DESC
        }

        @SerializedName("column_name")
        protected String columnName;

        @SerializedName("order")
        protected Order order = Order.ASC;

        public OrderBy(final String columnName, final Order order) {
            this.columnName = columnName;
            this.order = order;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public String getColumnName() {
            return columnName;
        }

        public Order getOrder() {
            return order;
        }
    }

    private final String tableName;

    private final String pkName;

    private Field pkField = null;

    private final Class<T> clazz;

    private final FieldAccessFactory<T> fieldAccessFactory;

    /**
     * @param conn
     * @param tableName
     * @param pkName
     * @param clazz
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    public RMBTRepository(Connection conn, String tableName, Class<T> clazz) {
        super(conn);

        try {
            for (final Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(DatabasePrimaryKey.class)) {
                    pkField = f;
                    pkField.setAccessible(true);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        if (pkField != null) {
            final SerializedName serializedName = pkField.getAnnotation(SerializedName.class);
            if (serializedName != null) {
                pkName = serializedName.value();
            } else {
                pkName = pkField.getName();
            }
        } else {
            pkName = null;
        }

        this.tableName = tableName;
        this.clazz = clazz;

        this.fieldAccessFactory = new FieldAccessFactory<T>(clazz);
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * for backwards compatibility in code (see {@link at.alladin.rmbt.shared.db.fields.Field})
     *
     * @param object
     * @return
     */
    public FieldAccessImpl<T> getFieldAccessImpl(final T object) {
        return new FieldAccessImpl<T>(object, fieldAccessFactory);
    }

    public String getInsertQuery(T object) {
        final String insertQuery;
        final StringBuilder sb = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        final StringBuilder sbQm = new StringBuilder("VALUES (");

        final JsonElement element = getGson().toJsonTree(object);
        final Iterator<Entry<String, JsonElement>> iterator = element.getAsJsonObject().entrySet().iterator();

        while (iterator.hasNext()) {
            final Entry<String, JsonElement> e = iterator.next();
            sb.append(e.getKey());
            sbQm.append("?");
            if (iterator.hasNext()) {
                sb.append(", ");
                sbQm.append(", ");
            }
        }

        sb.append(") ").append(sbQm.toString()).append(")");
        if (pkName != null) {
            sb.append(" RETURNING ").append(pkName);
        }
        insertQuery = sb.toString();

        return insertQuery;
    }


    public String getUpdateQuery(T object, Object uid) {
        final String updateQuery;
        final StringBuilder sb = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        final JsonElement element = gson.toJsonTree(object);
        final Iterator<Entry<String, JsonElement>> iterator = element.getAsJsonObject().entrySet().iterator();

        while (iterator.hasNext()) {
            final Entry<String, JsonElement> e = iterator.next();
            sb.append(e.getKey()).append("=?");
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append(" WHERE ").append(pkName).append("=").append(uid);
        updateQuery = sb.toString();

        return updateQuery;
    }

    public long save(T object) throws SQLException {
        final JsonElement element = getGson().toJsonTree(object);

        final Iterator<Entry<String, JsonElement>> iterator = element.getAsJsonObject().entrySet().iterator();

        String query = null;
        Object pkValue = null;

        if (pkField != null) {
            try {
                pkValue = pkField.get(object);
                if (pkValue != null) {
                    query = getUpdateQuery(object, pkValue);
                }
            } catch (final Exception e) {
                logger.error(e.getMessage());
            }
        }

        if (query == null) {
            query = getInsertQuery(object);
        }

        final PreparedStatement ps = getConnection().prepareStatement(query);

        int c = 1;
        while (iterator.hasNext()) {
            final Entry<String, JsonElement> e = iterator.next();
            if (e.getValue().isJsonPrimitive()) {
                final JsonPrimitive p = e.getValue().getAsJsonPrimitive();
                if (p.isBoolean()) {
                    ps.setBoolean(c++, p.getAsBoolean());
                } else if (p.isNumber()) {
                    ps.setBigDecimal(c++, p.getAsBigDecimal());
                } else if (p.isString()) {
                    ps.setObject(c++, p.getAsString(), Types.OTHER);
                } else if (p.isJsonNull()) {
                    ps.setNull(c++, Types.NULL);
                }
            } else if (e.getValue().isJsonNull()) {
                ps.setNull(c++, Types.NULL);
            } else if (e.getValue().isJsonArray()) {
                ps.setObject(c++, e.getValue(), Types.OTHER);
            }
        }

        if (pkValue == null) {
            final ResultSet keySet = ps.executeQuery();
            if (keySet.next()) {
                return keySet.getLong(1);
            }

            return 0;
        } else {
            logger.debug(ps.toString());
            return ps.executeUpdate();
        }
    }

    public T getByUid(long uid) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement("select row_to_json(" + tableName + ") from " + tableName + " where " + pkName + "=?");
        ps.setLong(1, uid);
        final ResultSet result = ps.executeQuery();

        if (result.next()) {
            final String jsonString = result.getString(1);
            final T object = getGson().fromJson(jsonString, clazz);
            return object;
        }

        return null;
    }

    public List<T> getByColumnName(String columnName, Object value) throws SQLException {
        return getByColumnNames(new String[]{columnName}, new Object[]{value}, null, null, null);
    }

    public List<T> getByColumnName(String columnName, Object value, final OrderBy[] order) throws SQLException {
        return getByColumnNames(new String[]{columnName}, new Object[]{value}, null, null, order);
    }

    public List<T> getByColumnName(String columnName, Object value, final Long limit, final Long offset, final OrderBy[] order) throws SQLException {
        return getByColumnNames(new String[]{columnName}, new Object[]{value}, limit, offset, order);
    }

    public List<T> getByColumnNames(final String[] columnNames, final Object[] values) throws SQLException {
        return getByColumnNames(columnNames, values, null, null, null);
    }

    public List<T> getByColumnNames(final String[] columnNames, final Object[] values, final OrderBy[] order) throws SQLException {
        return getByColumnNames(columnNames, values, null, null, order);
    }

    public List<T> getByColumnNames(final String[] columnNames, final Object[] values, final Long limit, final Long offset, final OrderBy[] order) throws SQLException {
        final StringBuilder query = new StringBuilder();
        query.append("select row_to_json(").append(tableName).append(") from ").append(tableName).append(" where ");
        for (int i = 0; i < columnNames.length; i++) {
            query.append(columnNames[i]).append(values[i] != null ? "=?" : " ISNULL ");
            if (i < columnNames.length - 1) {
                query.append(" and ");
            }
        }

        if (order != null && order.length > 0) {
            query.append(" order by ");
            for (int i = 0; i < order.length; i++) {
                query.append(order[i].getColumnName()).append(" ").append(order[i].getOrder().name());
                if (i < order.length - 1) {
                    query.append(", ");
                }
            }
        }

        if (limit != null) {
            query.append(" limit ").append(limit);
        }

        if (offset != null) {
            query.append(" offset ").append(offset);
        }

        final PreparedStatement ps = connection.prepareStatement(query.toString());

        int parameterIndex = 1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                if (values[i] instanceof String) {
                    ps.setObject(parameterIndex++, values[i], Types.OTHER);
                } else {
                    ps.setObject(parameterIndex++, values[i]);
                }
            }
        }

        final List<T> resultList = new ArrayList<T>();
        final ResultSet result = ps.executeQuery();
        while (result.next()) {
            resultList.add(getGson().fromJson(result.getString(1), clazz));
        }

        return resultList;
    }

    /**
     * get a list (or page) of elements from this table
     *
     * @param limit  limit the size of the list
     * @param offset the starting offset
     * @return
     * @throws SQLException
     */
    public List<T> getList(final Page page) throws SQLException {
        StringBuilder pageQuery = new StringBuilder();
        pageQuery.append("select row_to_json(").append(tableName).append(") from ").append(tableName);

        if (page != null) {
            if (page.getLimit() != null) {
                pageQuery.append(" limit ").append(page.getLimit());
            }

            if (page.getOffset() != null) {
                pageQuery.append(" offset ").append(page.getOffset());
            }
        }

        final PreparedStatement ps = getConnection().prepareStatement(pageQuery.toString());
        final ResultSet result = ps.executeQuery();

        final List<T> resultList = new ArrayList<T>();
        while (result.next()) {
            resultList.add(getGson().fromJson(result.getString(1), clazz));
        }

        return resultList;
    }
}
