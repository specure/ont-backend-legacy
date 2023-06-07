package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.shared.ResourceManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

/**
 * @author Tomas Hreben
 * @email tomas.hreben@martes-specure.com
 * @date 23.october 2017
 */
@Deprecated
public class CountriesResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(CountriesResource.class);

    private static final String SQL_ALL_COUNTRIES = "SELECT DISTINCT(country) FROM mcc2country ORDER BY country ASC";

//    private static final String SQL_ALL_COUNTRIES = "SELECT DISTINCT(country) FROM mcc2country WHERE (mcc in " +
//            "(select DISTINCT substr(mcc_mnc, 1, 3) from provider where mcc_mnc NOTNULL )) OR" +
//            "      (mcc2country.country IN (SELECT ac.country" +
//            "                   FROM provider p" +
//            "                     JOIN as2provider ap ON (p.uid = ap.provider_id) " +
//            "                     JOIN asn2country ac ON (ap.asn = ac.asn))) ORDER BY country ASC";

    @Post("json")
    @Get("json")
    public String request(String entity){

        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONArray answer = new JSONArray();
        JSONObject request = null;

        try{
            String lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
            List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

            if (entity != null) {
                request = new JSONObject(entity);

                lang = request.optString("language");
            }

            if (langs.contains(lang)) {
                labels = null;
                labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                countries = ResourceManager.getCountries(new Locale(lang));
            }

            PreparedStatement ps = conn.prepareStatement(SQL_ALL_COUNTRIES);

            logger.debug(ps.toString());
            ResultSet rs = ps.executeQuery();

            Collator collator = Collator.getInstance(new Locale(lang));
            collator.setStrength(Collator.PRIMARY);

            Map<String, String> map = new TreeMap<>(collator);

            while (rs.next()) {
                map.put(countries.getString("country_" + rs.getString("country")),
                        rs.getString("country"));
            }

            JSONObject obj = new JSONObject();
            obj.put("country_code", "all");
            obj.put("country_name", countries.getString("country_all"));
            answer.put(obj);

            Iterator iterator = map.entrySet().iterator();
            Map.Entry<String, String> resultMap = null;
            while (iterator.hasNext()){
                resultMap = (Map.Entry<String, String>)iterator.next();
                obj = new JSONObject();
                obj.put("country_name", resultMap.getKey());
                obj.put("country_code", resultMap.getValue());
                answer.put(obj);
            }

        } catch (SQLException sqlE){
            logger.error(sqlE.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }

}
