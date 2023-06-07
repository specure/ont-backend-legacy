package at.alladin.rmbt.controlServer.OffOnNet.repository;

import at.alladin.rmbt.controlServer.OffOnNet.model.MeasurementServerModel;
import at.alladin.rmbt.shared.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MeasurementServerPGRepository {
    private final Connection connection;
    private static final String SELECT_MAX_UID_FROM_TEST_SERVER = "SELECT COALESCE(max(uid),0) from test_server";

    private static final String GET_TEST_SERVER_BY_ID =
            "SELECT *, ts.uid as ts_uid, ts.name as ts_name, pr.uid as pr_uid, pr.name as pr_name\n" +
            "FROM test_server ts\n" +
            "left join test_servers_providers tsp on ts.uid = tsp.test_server_id\n" +
            "left join provider pr on tsp.provider_id = pr.uid\n" +
            "where ts.uid=?;";

    private static final String SELECT_SERVERS =
            "SELECT *, ts.uid as ts_uid, ts.name as ts_name, pr.uid as pr_uid, pr.name as pr_name\n" +
            "FROM test_server ts\n" +
            "left join test_servers_providers tsp on ts.uid = tsp.test_server_id\n" +
            "left join provider pr on tsp.provider_id = pr.uid\n" +
            "LIMIT ?";

    private static final String CREATE_SERVER =
            "INSERT INTO test_server(" +
                    "uid, name, web_address, city, country, geo_lat, " +
                    "geo_long, web_address_ipv4, web_address_ipv6, server_type, " +
                    "server_group, state, active, contact_email, contact_name, " +
                    "port, port_ssl)\n" +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?);";

    private static final String UPDATE_SERVER =
        "UPDATE test_server SET (" +
                "name, web_address, city, country, geo_lat, " +
                "geo_long, web_address_ipv4, web_address_ipv6, server_type, " +
                "server_group, state, active, contact_email, contact_name, " +
                "port, port_ssl) = (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) where uid=?";

    private static final String FIND_TEST_SERVER_BY_IP =
            "SELECT *, ts.uid as ts_uid, ts.name as ts_name, pr.uid as pr_uid, pr.name as pr_name\n" +
            "FROM test_server ts\n" +
            "left join test_servers_providers tsp on ts.uid = tsp.test_server_id\n" +
            "left join provider pr on tsp.provider_id = pr.uid\n" +
            "WHERE web_address = ? OR web_address_ipv4 = ? OR web_address_ipv6 = ?";

    public MeasurementServerPGRepository(Connection connection) {
        this.connection = connection;
    }

    public List<MeasurementServerModel> getAllLimitedTo(int limit) throws SQLException {
        ArrayList<MeasurementServerModel> result = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(SELECT_SERVERS);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            result.add(new MeasurementServerModel(rs));
        }
        SQLHelper.closeResultSet(rs);
        return result;
    }

    public MeasurementServerModel updateMeasurementServer(MeasurementServerModel data) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(UPDATE_SERVER);
        int i = 1;
        ps.setString(i++, data.getName());
        ps.setString(i++, data.getWebAddress());
        ps.setString(i++, data.getCity());
        ps.setString(i++, data.getCountry());
        ps.setDouble(i++, data.getLat());
        ps.setDouble(i++, data.getLng());
        ps.setString(i++, data.getWebAddressIpv4());
        ps.setString(i++, data.getWebAddressIpv6());
        ps.setString(i++, data.getServerType());
        ps.setInt(i++, data.getServerGroup());
        ps.setString(i++, data.getState());
        ps.setBoolean(i++, data.getActive());
        ps.setString(i++, data.getContactEmail());
        ps.setString(i++, data.getContactName());
        ps.setInt(i++, data.getPort());
        ps.setInt(i++, data.getPortSSL());

        ps.setInt(i, data.getUid());

        ps.executeUpdate();

        return getMeasurementServerById(data.getUid());
    }
    public MeasurementServerModel createMeasurementServer(MeasurementServerModel data) throws SQLException {
        ResultSet maxTestServerUidResult = connection
                .prepareStatement(SELECT_MAX_UID_FROM_TEST_SERVER)
                .executeQuery();

        if(maxTestServerUidResult == null || !maxTestServerUidResult.next()) {
            throw new SQLException("can't get max GUI for new testServer creation");
        }
        int uid = maxTestServerUidResult.getInt(1) + 1;

        PreparedStatement ps = connection.prepareStatement(CREATE_SERVER);
        ps.setInt(1, uid);
        ps.setString(2, data.getName());
        ps.setString(3, data.getWebAddress());
        ps.setString(4, data.getCity());
        ps.setString(5, data.getCountry());
        ps.setDouble(6, data.getLat());
        ps.setDouble(7, data.getLng());
        ps.setString(8, data.getWebAddressIpv4());
        ps.setString(9, data.getWebAddressIpv6());
        ps.setString(10, data.getServerType());
        ps.setInt(11, data.getServerGroup());
        ps.setString(12, data.getState());
        ps.setBoolean(13, data.getActive());
        ps.setString(14, data.getContactEmail());
        ps.setString(15, data.getContactName());
        ps.setInt(16, data.getPort());
        ps.setInt(17, data.getPortSSL());
        ps.executeUpdate();

        return getMeasurementServerById(uid);
    }

    public MeasurementServerModel getMeasurementServerById(int uid) throws SQLException {
        PreparedStatement getServerStatement = connection.prepareStatement(GET_TEST_SERVER_BY_ID);
        getServerStatement.setInt(1, uid);
        ResultSet serverResultSet = getServerStatement.executeQuery();
        if (!serverResultSet.next()) {
            return null;
        }
        MeasurementServerModel server = new MeasurementServerModel(serverResultSet);
        SQLHelper.closeResultSet(serverResultSet);
        return server;
    }
    public List<MeasurementServerModel> getByIP(String ip) throws  SQLException {
        ArrayList<MeasurementServerModel> result = new ArrayList<>();
        PreparedStatement findStatement = connection.prepareStatement(FIND_TEST_SERVER_BY_IP);

        findStatement.setString(1, ip);
        findStatement.setString(2, ip);
        findStatement.setString(3, ip);

        ResultSet resultSet = findStatement.executeQuery();
        while (resultSet.next()) {
            result.add(new MeasurementServerModel(resultSet));
        }
        SQLHelper.closeResultSet(resultSet);
        return result;
    }
}
