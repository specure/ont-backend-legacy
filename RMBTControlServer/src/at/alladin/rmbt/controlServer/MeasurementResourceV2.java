package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.json.LocationJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
import at.alladin.rmbt.shared.json.TestServerJsonV2;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Tomas Hreben
 * @date 14.november 2017
 */
public class MeasurementResourceV2 extends MeasurementResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(MeasurementResourceV2.class);

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();

        JSONObject request = null;
        try {
            request = new JSONObject(entity);
        } catch (JSONException e) {
            logger.error("Error parsing JSDON Data " + e.toString());
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            errorList.addError("ERROR_REQUEST_JSON");
            answer.putOpt("error", errorList.getList());
            logger.error("rsponse: " + answer.toString());
            return answer.toString();
        }

        String lang = request.optString("language");

        // Load Language Files for Client

        final List<String> langs =
                Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

        if (langs.contains(lang)) {
            errorList.setLanguage(lang);
            labels = ResourceManager.getSysMsgBundle(new Locale(lang));
        }

        String clientIpRaw = this.getIP();
        InetAddress clientAddress = InetAddresses.forString(clientIpRaw);
        String clientIpString = InetAddresses.toAddrString(clientAddress);
        String geoIpCountry = GeoIPHelper.getInstance().lookupCountry(clientAddress);

        logger.debug("ClientAddress: " + clientAddress);
        logger.debug("geoIpCountry: " + geoIpCountry);

        Long asn = Helperfunctions.getASN(clientAddress);

        String asCountry;
        if (asn == null) {
            asCountry = null;
        } else {
            asCountry = Helperfunctions.getAScountry(asn);
        }


        String serverType = null;
        // TODO: rewrite add QoS
        if (request.optString("client").equals("RMBTws")) {
            serverType = "RMBTws";
        } else {
            serverType = "RMBT";
        }

        // check if user uses ipv6 or no
        Boolean ipv6 = null;
        if (clientAddress instanceof Inet6Address) {
            ipv6 = true;
        } else if (clientAddress instanceof Inet4Address) {
            ipv6 = false;
        } else { // should never happen, unless ipv > 6 is available
            ipv6 = null;
        }
        LocationJson locationJson = null;
        if (request.has("location")) {
            locationJson = new LocationJson(request.getJSONObject("location"));
        } else {
            locationJson = new LocationJson();
            locationJson.setGeo_lat((double) GeoIPHelper.getInstance().getLatitudeFromIP(clientAddress));
            locationJson.setGeo_long((double) GeoIPHelper.getInstance().getLongitudeFromIP(clientAddress));
        }

        logger.debug("LocationJson: " + locationJson.toString());

        List<TestServerJsonInterface> testServerJsons = getNearestServer(locationJson.getGeo_lat(),
                locationJson.getGeo_long(), clientIpString, asCountry, geoIpCountry, serverType, true, ipv6);

        try {
            JSONArray servers = new JSONArray();
            JSONObject server = null;
            if (testServerJsons != null && testServerJsons.isEmpty() == false) {
                for (TestServerJsonInterface tsj : testServerJsons) {
                    server = new JSONObject();
                    server.put("id", ((TestServerJsonV2) tsj).getId());
                    server.put("sponsor", ((TestServerJsonV2) tsj).getSponsor());
                    server.put("port", ((TestServerJsonV2) tsj).getPort());
                    server.put("address", ((TestServerJsonV2) tsj).getAddress());
                    server.put("city", ((TestServerJsonV2) tsj).getCity());
                    server.put("country", ((TestServerJsonV2) tsj).getState());
                    server.put("distance", ((TestServerJsonV2) tsj).getDistance() + " " + labels.getString("distance"));
                    servers.put(server);
                }
            } else {
                errorList.addError("ERROR_TEST_SERVER");
            }

            answer.put("servers", servers);
            answer.putOpt("error", errorList.getList());

            // log response
            logger.debug("rsponse: " + answer.toString());

            return answer.toString();
        } catch (JSONException e) {
            logger.error(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            errorList.addError("ERROR_DB_GET_SERVER");
            answer.putOpt("error", errorList.getList());
            logger.error("rsponse: " + answer.toString());
            return answer.toString();
        }
    }

    @Override
    public List<TestServerJsonInterface> getNearestServer(Double geolat, Double geolong, String clientIp,
                                                          String asCountry, String geoIpCountry, String serverType, boolean ssl, Boolean ipv6) {
        String address;

        List<TestServerJsonInterface> testServers = new LinkedList<>();

        PreparedStatement ps = null;

        try {
            // use geoIP with fallback to AS
            String country = asCountry;
            if (!Strings.isNullOrEmpty(geoIpCountry))
                country = geoIpCountry;

            int i = 1;

            if (!(geolat == Double.MAX_VALUE && geolong == Double.MAX_VALUE)) {
                // We will find by geo location
                ps = conn.prepareStatement(ServerResource.getSqlTestServerWithGps());
                ps.setDouble(i++, geolong);
                ps.setDouble(i++, geolat);
                ps.setString(i++, serverType);
                ps.setString(i++, country);
            } else {
                ps = conn.prepareStatement(ServerResource.getSqlTestServerWithoutGps());
                ps.setString(i++, serverType);
                ps.setString(i++, country);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    if (ipv6 == null) {
                        address = rs.getString("web_address");
                    } else if (ipv6) {
                        address = rs.getString("web_address_ipv6");
                        if(address == null){
                            address = rs.getString("web_address");
                        }
                    } else {
                        address = rs.getString("web_address_ipv4");
                    }

                    testServers.add(new TestServerJsonV2(rs.getInt("uid"),
                            rs.getString("name"),
                            rs.getInt(ssl ? "port_ssl" : "port"), address, rs.getFloat("distance"),
                            rs.getString("state"), rs.getString("city")));
                }

                return testServers;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return null;
        }

    }

}
