package at.alladin.rmbt.controlServer.OffOnNet.model;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProviderModel {
    private final int uid;
    private final String name;
    private final String mcc_mnc;
    private final String shortName;
    private final boolean mapFilter;

    public ProviderModel(ResultSet rs) throws SQLException {
        uid = rs.getInt("uid");
        name = rs.getString("name");
        mcc_mnc = rs.getString("mcc_mnc");
        shortName = rs.getString("shortname");
        mapFilter = rs.getBoolean("map_filter");
    }
    public JSONObject asJsonObject() {
        JSONObject provider = new JSONObject();
        provider.put("uid", uid);
        provider.put("name", name);
        provider.put("mcc_mnc", mcc_mnc);
        provider.put("shortname", shortName);
        provider.put("map_filter", mapFilter);
        return provider;
    }
    public int getUid() {
        return uid;
    }
    public String getName() {
        return name;
    }
    public String getMcc_mnc() {
        return mcc_mnc;
    }
    public String getShortName() {
        return shortName;
    }
    public boolean getMapFilter() {
        return mapFilter;
    }
}
