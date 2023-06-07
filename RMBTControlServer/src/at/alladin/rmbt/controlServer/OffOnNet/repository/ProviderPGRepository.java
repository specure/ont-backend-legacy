package at.alladin.rmbt.controlServer.OffOnNet.repository;

import at.alladin.rmbt.controlServer.OffOnNet.model.ProviderModel;
import at.alladin.rmbt.shared.SQLHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProviderPGRepository {

    private final Connection connection;

    private static final String SELECT_MAX_UID_FROM_PROVIDER =
            "SELECT COALESCE(max(uid),0) from provider";

    private static final String CREATE_PROVIDER =
            "INSERT INTO provider(uid, name, mcc_mnc, shortname, map_filter) " +
            "VALUES (?,?,?,?,?)";

    private static final String GET_PROVIDER =
            "SELECT * from provider limit ?;";

    private static final String GET_PROVIDER_BY_NAME =
            "SELECT * from provider where name = ?";

    private static final String GET_PROVIDER_BY_SHORT_NAME =
            "SELECT * from provider where shortname = ?";

    public ProviderPGRepository(Connection connection) {
        this.connection = connection;
    }

    public void createNewProvider(ProviderModel provider) throws SQLException {
        int uid = getUid();
        PreparedStatement ps = connection.prepareStatement(CREATE_PROVIDER);
        ps.setInt(1, uid);
        ps.setString(2, provider.getName());
        ps.setString(3, provider.getMcc_mnc());
        ps.setString(4, provider.getShortName());
        ps.setBoolean(5, provider.getMapFilter());

        ps.executeUpdate();
    }

    private int getUid() throws SQLException {
        ResultSet maxProviderUidResult = connection
                .prepareStatement(SELECT_MAX_UID_FROM_PROVIDER)
                .executeQuery();
        if(maxProviderUidResult == null || !maxProviderUidResult.next()) {
            throw new SQLException("can't get max GUI for new provider");
        }
        return maxProviderUidResult.getInt(1) + 1;
    }

    public List<ProviderModel> getAllProvidersLimitedTo(int limit) throws SQLException {
        List<ProviderModel> result = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(GET_PROVIDER);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(new ProviderModel(rs));
        }
        SQLHelper.closeResultSet(rs);
        return result;
    }

    public List<ProviderModel> getByName(String providerName) throws SQLException {
        return getByQueryWithOneStringParameter(GET_PROVIDER_BY_NAME, providerName);
    }

    public List<ProviderModel>  getByShortName(String providerShortName) throws SQLException {
        return getByQueryWithOneStringParameter(GET_PROVIDER_BY_SHORT_NAME, providerShortName);
    }
    private List<ProviderModel> getByQueryWithOneStringParameter(String query, String param) throws SQLException {
        List<ProviderModel> result = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, param);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(new ProviderModel(rs));
        }
        SQLHelper.closeResultSet(rs);
        return result;
    }
}
