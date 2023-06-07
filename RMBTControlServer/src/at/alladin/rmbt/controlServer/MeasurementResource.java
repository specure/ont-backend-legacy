package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.json.LocationJson;
import at.alladin.rmbt.shared.json.TestServerJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
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
import java.util.List;


/**
 * @author tomas.hreben
 * @date 25.7.2017
 */
public class MeasurementResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(MeasurementResource.class);

    public MeasurementResource() {
    }

//    public MeasurementResource(Connection conn) {
//        this.conn = conn;
//    }

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject answer = new JSONObject();
        ErrorList errorList = new ErrorList();

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
        if (request.has("location") && request.getJSONObject("location").has("geo_lat") && request.getJSONObject("location").has("geo_long")) {
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
                    server.put("id", ((TestServerJson) tsj).getId());
                    server.put("name", ((TestServerJson) tsj).getName());
                    server.put("port", ((TestServerJson) tsj).getPort());
                    server.put("address", ((TestServerJson) tsj).getAddress());
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


}
