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
import java.util.UUID;

public class Client {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final String SQL_SELECT_CLIENT =
            "SELECT client.*, client_type.name AS client_type_name "
                    + "FROM client , client_type "
                    + "WHERE client.client_type_id = client_type.uid AND client.uuid = CAST ( ? AS uuid)";

    private static final String SQL_SELECT_CLIENT_BY_UID =
            "SELECT client.*, client_type.name AS client_type_name "
                    + "FROM client , client_type WHERE client.client_type_id = client_type.uid AND client.uid = ?";

    private static final String SQL_INSERT_CLIENT =
            "INSERT INTO client(uuid, client_type_id, time, sync_group_id, sync_code, terms_and_conditions_accepted)"
                    + "VALUES( CAST( ? AS UUID), ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_CLIENT_UID = "SELECT uid FROM client_type WHERE name = ?";

    private long uid;
    private UUID uuid;
    private int client_type_id;
    private String client_type_name;
    private Timestamp time;
    private Calendar time_zone;
    private int sync_group_id;
    private String sync_code;

    private Connection conn = null;

    private String errorLabel = "";

    private boolean error = false;
    private boolean tcAccepted;

    public Client(final Connection conn) {
        reset();
        this.conn = conn;
    }

    public Client(final Connection conn, final long uid, final UUID uuid, final int client_type_id,
                  final Timestamp time, final Calendar time_zone, final int sync_group_id, final String sync_code,
                  final boolean tcAccepted) {
        reset();
        this.conn = conn;

        this.uid = uid;
        this.uuid = uuid;
        this.client_type_id = client_type_id;
        this.time = time;
        this.time_zone = time_zone;
        this.sync_group_id = sync_group_id;
        this.sync_code = sync_code;
        this.tcAccepted = tcAccepted;
    }

    public void reset() {
        uid = 0;
        uuid = null;
        client_type_id = 0;
        client_type_name = "";
        time = null;
        time_zone = null;
        sync_group_id = 0;
        sync_code = "";
        tcAccepted = false;
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

    public long getClientByUuid(final UUID uuid) {
        resetError();

        try {

            final PreparedStatement st = conn
                    .prepareStatement(SQL_SELECT_CLIENT);
            st.setString(1, uuid.toString());

            final ResultSet rs = st.executeQuery();

            if (rs.next()) {
                uid = rs.getLong("uid");
                this.uuid = UUID.fromString(rs.getString("uuid"));
                client_type_id = rs.getInt("client_type_id");
                client_type_name = rs.getString("client_type_name");
                time = rs.getTimestamp("time");
                time_zone = Helperfunctions.getTimeWithTimeZone(Helperfunctions.getTimezoneId());
                sync_group_id = rs.getInt("sync_group_id");
                sync_code = rs.getString("sync_code");
                tcAccepted = rs.getBoolean("terms_and_conditions_accepted");
            } else {
                // setError("ERROR_DB_GET_CLIENT");
                // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
                // new Object[] {uuid}));
            }

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            setError("ERROR_DB_GET_CLIENT_SQL");
            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
        }

        return uid;
    }

    public boolean getClientByUid(final long uid) {
        resetError();

        try {

            final PreparedStatement st = conn
                    .prepareStatement(SQL_SELECT_CLIENT_BY_UID);
            st.setLong(1, uid);

            final ResultSet rs = st.executeQuery();

            if (rs.next()) {
                this.uid = rs.getLong("uid");
                uuid = UUID.fromString(rs.getString("uuid"));
                client_type_id = rs.getInt("client_type_id");
                client_type_name = rs.getString("client_type_name");
                time = rs.getTimestamp("time");
                sync_group_id = rs.getInt("sync_group_id");
                sync_code = rs.getString("sync_code");
                tcAccepted = rs.getBoolean("terms_and_conditions_accepted");
            } else
                setError("ERROR_DB_GET_CLIENT");
            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENT"),
            // new Object[] {uuid}));

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            setError("ERROR_DB_GET_CLIENT_SQL");
            // errorList.addError("ERROR_DB_GET_CLIENT_SQL");
        }

        return !error;
    }

    public UUID storeClient() {

        resetError();

        try {
            PreparedStatement st;
            uuid = UUID.randomUUID();

            st = conn.prepareStatement(
                    SQL_INSERT_CLIENT, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, uuid.toString());
            st.setInt(2, client_type_id);
            st.setTimestamp(3, time);
            if (sync_group_id > 0)
                st.setInt(4, sync_group_id);
            else
                st.setObject(4, null);
            if (sync_code.length() > 0)
                st.setString(5, sync_code);
            else
                st.setObject(5, null);

            st.setBoolean(6, tcAccepted);

            logger.debug(st.toString());
            final int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                uid = 0;
                uuid = null;
                client_type_id = 0;
                client_type_name = "";
                time = null;
                setError("ERROR_DB_STORE_CLIENT");
                // errorList.addError(labels.getString("ERROR_DB_STORE_CLIENT"));
            } else {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next())
                    // Retrieve the auto generated key(s).
                    uid = rs.getLong(1);
            }

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            setError("ERROR_DB_STORE_CLIENT_SQL");
            // errorList.addError(labels.getString("ERROR_DB_STORE_CLIENT_SQL"));
            logger.error(e.getMessage());
        }

        return uuid;
    }

    public int getTypeId(final String clientType) {
        resetError();
        int id = 0;

        try {

            final PreparedStatement st = conn.prepareStatement(SQL_SELECT_CLIENT_UID);
            st.setString(1, clientType.toUpperCase());
            final ResultSet rs = st.executeQuery();

            if (rs.next())
                id = rs.getInt(1);
            else
                setError("ERROR_DB_GET_CLIENTTYPE");
            // errorList.addError(MessageFormat.format(labels.getString("ERROR_DB_GET_CLIENTTYPE"),
            // new Object[] {clientType}));

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            setError("ERROR_DB_GET_CLIENTTYPE_SQL");
            // errorList.addError(labels.getString("ERROR_DB_GET_CLIENTTYPE_SQL"));
            logger.error(e.getMessage());
        }

        return id;
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

    public UUID getUuid() {
        return uuid;
    }

    public int getClient_type_id() {
        return client_type_id;
    }

    public String getClient_type_name() {
        return client_type_name;
    }

    public Timestamp getTime() {
        return time;
    }

    public Calendar getTimeZone() {
        return time_zone;
    }

    public void setUid(final long uid) {
        this.uid = uid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public int getSync_group_id() {
        return sync_group_id;
    }

    public String getSync_code() {
        return sync_code;
    }

    public void setTcAccepted(final boolean tcAccepted) {
        this.tcAccepted = tcAccepted;
    }

    public boolean isTcAccepted() {
        return tcAccepted;
    }

    public void setClient_type_id(final int client_type_id) {
        this.client_type_id = client_type_id;
    }

    public void setTime(final Timestamp time) {
        this.time = time;
    }

    public void setTimeZone(final Calendar time_zone) {
        this.time_zone = time_zone;
    }

    public void setSync_group_id(final int sync_group_id) {
        this.sync_group_id = sync_group_id;
    }

    public void setSync_code(final String sync_code) {
        this.sync_code = sync_code;
    }
}
