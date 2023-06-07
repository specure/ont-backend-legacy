package at.alladin.rmbt.controlServer.OffOnNet.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeasurementServerModel {
    private final int uid;
    private final String name;
    private final String webAddress;
    private final String city;
    private String country;
    private final Double lat;
    private final Double lng;
    private String webAddressIpv4;
    private String webAddressIpv6;
    private final String serverType;
    private final String uuid;
    private final int serverGroup;
    private int port;
    private int portSSL;
    private final String state;

    // get from external table: test_servers_providers
    private int providerId;
    private String providerShortName;
    private String providerName;

    private String contactName;
    private String contactEmail;

    Boolean active = false;
    
    public MeasurementServerModel(ResultSet rs) throws SQLException {
        this.uid = rs.getInt("ts_uid");
        this.name = rs.getString("ts_name");
        this.webAddress = rs.getString("web_address");
        this.providerId = rs.getInt("pr_uid");
        this.providerShortName = rs.getString("shortname");
        this.providerName = rs.getString("pr_name");
        this.city = rs.getString("city");
        this.country = rs.getString("country");
        this.lat = rs.getDouble("geo_lat");
        this.lng = rs.getDouble("geo_long");
        this.webAddressIpv4 = rs.getString("web_address_ipv4");
        this.webAddressIpv6 = rs.getString("web_address_ipv6");
        this.serverType = rs.getString("server_type");
        this.uuid = rs.getString("uuid");

        this.serverGroup = rs.getInt("server_group");
        this.port = rs.getInt("port");
        this.portSSL = rs.getInt("port_ssl");

        this.state = rs.getString("state");

        this.active = rs.getBoolean("active");

        this.contactName = rs.getString("contact_name");
        this.contactEmail = rs.getString("contact_email");

    }
    public MeasurementServerModel(JSONObject obj) throws JSONException {
        if (obj.has("uid")) {
            this.uid = obj.getInt("uid"); // uеудуpdate
        } else {
            this.uid = 0; // will create new
        }
        this.name = obj.getString("name");
        this.webAddress = obj.getString("webAddress");

        if (obj.has("providerId")) {
            this.providerId = obj.getInt("providerId"); // will try to find
        } else {
            this.providerId = 0; // will create new
        }
        if (obj.has("providerShortName")) {
            this.providerShortName = obj.getString("providerShortName");
        }
        if (obj.has("providerName")) {
            this.providerName = obj.getString("providerName");
        }
        this.city = obj.getString("city");
        this.country = obj.getString("country");
        this.lat = obj.getDouble("lat");
        this.lng = obj.getDouble("lng");
        if (obj.has("webAddressIpv4")) {
            this.webAddressIpv4 = obj.getString("webAddressIpv4");
        }
        if (obj.has("webAddressIpv6")) {
            this.webAddressIpv6 = obj.getString("webAddressIpv6");
        }
        this.serverType = obj.getString("serverType");
        this.uuid = obj.getString("uuid");
        this.serverGroup = obj.getInt("serverGroup");
        if(obj.has("port")) {
            this.port = obj.getInt("port");
        }
        if(obj.has("port_ssl")) {
            this.portSSL = obj.getInt("port_ssl");
        }
        this.state = obj.getString("state");
        if (obj.has("active")) {
            this.active = obj.getBoolean("active");
        }
        if (obj.has("contactEmail")) {
            this.contactEmail = obj.getString("contactEmail");
        }
        if (obj.has("contactName")) {
            this.contactName = obj.getString("contactName");
        }
    }

    public JSONObject asJsonObject() {
        JSONObject server = new JSONObject();
        server.put("uid", uid);
        server.put("name", name);
        server.put("webAddress", webAddress);

        server.put("city", city);
        server.put("country", country);
        server.put("lat", lat);
        server.put("lng", lng);
        server.put("webAddressIpv4", webAddressIpv4);
        server.put("webAddressIpv6", webAddressIpv6);
        server.put("serverType", serverType);
        server.put("port", port);
        server.put("port_ssl", portSSL);
        server.put("uuid", uuid);
        server.put("serverGroup", serverGroup);
        server.put("state", state);
        server.put("active", active);

        server.put("contactEmail", contactEmail);
        server.put("contactName", contactName);

        // from external table
        server.put("providerId", providerId);
        server.put("providerShortName", providerShortName);
        server.put("providerName", providerName);

        return server;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public int getPort() {
        return port;
    }

    public int getPortSSL() {
        return portSSL;
    }

    public int getProviderId() {
        return providerId;
    }

    public String getProviderShortName() {
        return providerShortName;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getWebAddressIpv4() {
        return webAddressIpv4;
    }

    public String getWebAddressIpv6() {
        return webAddressIpv6;
    }

    public String getServerType() {
        return serverType;
    }

    public String getUuid() {
        return uuid;
    }

    public int getServerGroup() {
        return serverGroup;
    }

    public String getState() {
        return state;
    }

    public Boolean getActive() {
        return active;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }
}
