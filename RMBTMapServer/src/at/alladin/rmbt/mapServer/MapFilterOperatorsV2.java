package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class MapFilterOperatorsV2 extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(MapFilterOperatorsV2.class);

    private static final String FILTER_TYPE_ALL = "MFT_ALL";
    private static final String FILTER_TYPE_MOBILE = "MFT_MOBILE";
    private static final String FILTER_TYPE_BROWSER = "MFT_BROWSER";
    private static final String FILTER_TYPE_WLAN = "MFT_WLAN";


    private static final String SQL_SELECT_PROVIDER_WITHOUT_MCCMNC = "SELECT DISTINCT" +
            "  (p.shortname) shortname, p.name AS name, p.uid as uid " +
            "FROM provider p join as2provider ap on(p.uid = ap.provider_id) join asn2country ac on(ap.asn = ac.asn) " +
            "WHERE ac.country = ? AND p.mcc_mnc is null ORDER BY shortname ASC";

    private static final String SQL_SELECT_PROVIDER_BOTH = "SELECT DISTINCT(p.shortname) AS shortname, p.name AS name, p.uid as uid " +
            "FROM provider p WHERE substr(p.mcc_mnc, 1, 3) in (SELECT mcc FROM mcc2country WHERE mcc2country.country = ?) AND p.map_filter = TRUE " +
            " OR p.uid in (SELECT provider_id from as2provider ap join asn2country ac on (ap.asn = ac.asn) where ac.country = ?)" +
            " ORDER BY shortname ASC";

    private static final String SQL_SELECT_PROVIDER_WITH_MCCMNC = "SELECT DISTINCT(p.shortname) shortname,p.name as name, p.uid as uid  "
            + "FROM provider p JOIN mcc2country m ON (substr(p.mcc_mnc, 1, 3) = m.mcc)" +
            "WHERE m.country = ? AND p.map_filter = TRUE ORDER BY shortname ASC";

    @Post("json")
    @Get("json")
    public String request(final String entity) {
        // logger
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;
        JSONArray options = new JSONArray();

        String country = null;

        try {
            String lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

            if (entity != null) {
                request = new JSONObject(entity);
                //System.out.println(request);

                lang = request.optString("language");

                // Load Language Files for Client
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    labels = null;
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                    countries = ResourceManager.getSysMsgBundle(new Locale(lang));
                }
            }

            if (request == null) {
                request = new JSONObject(entity);
            }

            String providerType = request.getString("provider_type");
            country = request.getString("country_code");

            PreparedStatement ps = null;
            if (!country.equalsIgnoreCase("all")) {
                if (providerType.equals(FILTER_TYPE_MOBILE)) {
                    ps = conn.prepareStatement(SQL_SELECT_PROVIDER_WITH_MCCMNC);
                    ps.setString(1, country);
                } else if (providerType.equals(FILTER_TYPE_BROWSER) || providerType.equals(FILTER_TYPE_ALL)) {
                    ps = conn.prepareStatement(SQL_SELECT_PROVIDER_BOTH);
                    ps.setString(1, country);
                    ps.setString(2, country);
                } else if (providerType.equals(FILTER_TYPE_WLAN)) {
                    ps = conn.prepareStatement(SQL_SELECT_PROVIDER_WITHOUT_MCCMNC);
                    ps.setString(1, country);
                }
            }

            ResultSet rs = null;
            if (!country.equalsIgnoreCase("all")) {
                logger.debug(ps.toString());
                rs = ps.executeQuery();
            }

            JSONObject obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_ALL_OPERATORS"));
            obj.put("detail", "");
            obj.put("default", true);
            options.put(obj);
            while (rs != null && rs.next()) {
                obj = new JSONObject();
                obj.put("title", rs.getString("shortname"));
                obj.put("detail", rs.getString("name"));
                obj.put("provider", rs.getLong("uid"));
                obj.put("id_provider", rs.getString("shortname"));
                options.put(obj);
            }

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(ps);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        JSONObject result = new JSONObject();

        result.put("title", labels.getString("MAP_FILTER_CARRIER"));
        result.put("options", options);

        logger.debug("rsponse: " + result.toString());

        return result.toString();
    }
}
