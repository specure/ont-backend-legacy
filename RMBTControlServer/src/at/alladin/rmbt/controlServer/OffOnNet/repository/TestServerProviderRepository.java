package at.alladin.rmbt.controlServer.OffOnNet.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestServerProviderRepository {
    private final Connection connection;

    private static final String SELECT_MAX_UID_FROM_TEST_SERVER_PROVIDER =
            "SELECT COALESCE(max(uid),0) from test_servers_providers";

    private static final String CREATE_TEST_SERVER_PROVIDER =
            "INSERT INTO test_servers_providers(uid, provider_id, test_server_id) VALUES (?,?,?);";

    private static final String CLEAN_ALL_BY_TEST_SERVER_ID =
            "DELETE from test_servers_providers where test_server_id = ?;";

    public TestServerProviderRepository(Connection connection) {
        this.connection = connection;
    }
    public void createProviderAndTestServerRelation(int providerId, int testServerId) throws SQLException {
        int uid = getUID();

        PreparedStatement ps = connection.prepareStatement(CREATE_TEST_SERVER_PROVIDER);

        ps.setInt(1, uid);
        ps.setInt(2, providerId);
        ps.setInt(3, testServerId);

        ps.executeUpdate();

    }
    public void updateProviderAndTestServerRelation(int providerId, int testServerId) throws SQLException {

        // clean all for the server, only one provider model implemented here
        PreparedStatement cleanStatement = connection.prepareStatement(CLEAN_ALL_BY_TEST_SERVER_ID);
        cleanStatement.setInt(1, testServerId);
        cleanStatement.executeUpdate();

        // set up new connection
        createProviderAndTestServerRelation(providerId, testServerId);
    }

    private int getUID() throws SQLException {
        ResultSet maxTestServerUidResult = connection
                .prepareStatement(SELECT_MAX_UID_FROM_TEST_SERVER_PROVIDER)
                .executeQuery();

        if(maxTestServerUidResult == null || !maxTestServerUidResult.next()) {
            throw new SQLException("can't get max GUI for new testServer creation");
        }
        return maxTestServerUidResult.getInt(1) + 1;
    }
}
