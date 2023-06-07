/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.mapServer.MapServerOptions.MapOption;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.*;

public class InfoResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(InfoResource.class);

    // show countries in map filter( only SPECURE)
    private final Boolean showCountriesInMapFilter = CustomerResource.getInstance().showCountriesInMapFilter();

    private static final String SQL_SELECT_PROVIDER_WITH_MCC_MCC =
            "SELECT uid,name,mcc_mnc,shortname FROM provider p WHERE p.map_filter=true"
                    + " AND p.mcc_mnc IS NOT NULL ORDER BY p.shortname";

    private static final String SQL_SELECT_PROVIDER_WITHOUT_MCC_MCC =
            "SELECT uid,name,mcc_mnc,shortname FROM provider p WHERE p.map_filter=true"
                    + " ORDER BY p.shortname";

    private static final String SQL_SELECT_DEVICE_INFO =
            "SELECT string_agg(DISTINCT s.model,';') keys, COALESCE(adm.fullname, s.model) val"
                    + " FROM" + " (SELECT DISTINCT model FROM test t "
//                    + " WHERE t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'"
                    + " WHERE t.deleted = false AND t.status = 'FINISHED' AND t.time IS NOT NULL and t.timezone IS NOT NULL"
                    + " AND t.model IS NOT NULL %s) s"
                    + " LEFT JOIN device_map adm ON adm.codename=s.model GROUP BY val ORDER BY val ASC";

    private static final String SQL_ALL_COUNTRIES = "SELECT DISTINCT(country) FROM mcc2country ORDER BY country ASC";


    private JSONArray getCountries(String entity) throws JSONException {

        JSONArray answer = new JSONArray();
        JSONObject request = null;

        try {
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

//            obj = new JSONObject();
//            obj.put("country_code", "0");
//            obj.put("country_name", countries.getString("country_0"));
//            answer.put(obj);

            Iterator iterator = map.entrySet().iterator();
            Map.Entry<String, String> resultMap = null;
            while (iterator.hasNext()){
                resultMap = (Map.Entry<String, String>)iterator.next();
                obj = new JSONObject();
                obj.put("country_name", resultMap.getKey());
                obj.put("country_code", resultMap.getValue());
                answer.put(obj);
            }

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(ps);

        } catch (SQLException sqlE) {
            logger.error(sqlE.getMessage());
        }

        logger.debug("rsponse: " + answer.toString());

        return answer;
    }

    private JSONArray getMapTypeList() throws JSONException {
        final JSONArray result = new JSONArray();

        String lastType = null;
        JSONArray optionsArray = null;

        final Map<String, MapOption> mapOptionMap = MapServerOptions.getInstance().getMapOptionMap();
        for (final Map.Entry<String, MapOption> entry : mapOptionMap.entrySet()) {
            final String key = entry.getKey();
            final MapOption mapOption = entry.getValue();
            final String[] split = key.split("/");
            if (lastType == null || !lastType.equals(split[0])) {
                lastType = split[0];
                final JSONObject obj = new JSONObject();
                result.put(obj);
                optionsArray = new JSONArray();
                obj.put("options", optionsArray);
                obj.put("title", labels.getString(String.format("MAP_%s", lastType.toUpperCase())));
            }

            final JSONObject obj = new JSONObject();
            optionsArray.put(obj);
            obj.put("map_options", key);
            final String type = split[1].toUpperCase();
            obj.put("summary", labels.getString(String.format("MAP_%s_SUMMARY", type)));
            obj.put("title", labels.getString(String.format("RESULT_%s", type)));
            obj.put("unit", labels.getString(String.format("RESULT_%s_UNIT", type)));
            obj.put("heatmap_colors", mapOption.colorsHexStrings);
            obj.put("heatmap_captions", mapOption.captions);
            obj.put("classification", mapOption.classificationCaptions);
            obj.put("overlay_type", mapOption.overlayType);
        }
        //System.out.println(result);
        return result;
    }

    private JSONObject getMapFilterList() throws JSONException, SQLException {
        final JSONObject result = new JSONObject();

//        final JSONArray mapFilterStatisticalMethodList = new JSONArray();
//        final double[] statisticalMethodArray = {0.8, 0.5, 0.2};
//        for (int stat = 1; stat <= statisticalMethodArray.length; stat++) {
//
//            final JSONObject obj = new JSONObject();
//            obj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_TITLE"));
//            obj.put("summary", labels.getString("MAP_FILTER_STATISTICAL_METHOD_" + stat + "_SUMMARY"));
//            obj.put("statistical_method", statisticalMethodArray[stat - 1]);
//            if (stat == 2)  //2nd list entry is default (median)
//                obj.put("default", true);
//            mapFilterStatisticalMethodList.put(obj);
//        }
//
//        final JSONObject statisticalMethodObj = new JSONObject();
//        statisticalMethodObj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD"));
//        statisticalMethodObj.put("type", "MFT_MAP_STATISTIC_TYPE");
//        statisticalMethodObj.put("options", mapFilterStatisticalMethodList);

        JSONArray filterList = new JSONArray();
        result.put("mobile", filterList);
//        filterList.put(statisticalMethodObj);
        filterList.put(getOperators(true));
        filterList.put(getTimes());
        filterList.put(getTechnology());
//        filterList.put(getDevices("mobile"));

        final JSONObject operatorsNotMobile = getOperators(false);
        filterList = new JSONArray();
        result.put("wifi", filterList);
//        filterList.put(statisticalMethodObj);
        filterList.put(operatorsNotMobile);
        filterList.put(getTimes());
//        filterList.put(getDevices("wifi"));

        filterList = new JSONArray();
        result.put("browser", filterList);
//        filterList.put(statisticalMethodObj);
        filterList.put(operatorsNotMobile);
        filterList.put(getTimes());
//        filterList.put(getDevices("browser"));

        filterList = new JSONArray();
        result.put("all", filterList);
//        filterList.put(statisticalMethodObj);
        filterList.put(getTimes());

        return result;
    }

    private JSONObject getTimes() throws JSONException {
        final JSONArray options = new JSONArray();

        JSONObject obj;
        //   options.put(obj);
        //   obj.put("title", labels.getString("MAP_FILTER_PERIOD_1_DAY"));
        //   obj.put("summary", labels.getString("MAP_FILTER_PERIOD_1_DAY"));
        //   obj.put("period", 1);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_7_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_7_DAYS"));
        obj.put("period", 7);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_30_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_30_DAYS"));
        obj.put("period", 30);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_90_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_90_DAYS"));
        obj.put("period", 90);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_180_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_180_DAYS"));
        obj.put("default", true);
        obj.put("period", 180);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_365_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_365_DAYS"));
        obj.put("period", 365);

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_730_DAYS"));
        obj.put("summary", labels.getString("MAP_FILTER_PERIOD_730_DAYS"));
        obj.put("period", 730);

        final JSONObject result = new JSONObject();

        result.put("title", labels.getString("MAP_FILTER_PERIOD"));
        result.put("type", "MFT_PERIOD");
        result.put("options", options);

        return result;
    }

    private JSONObject getTechnology() throws JSONException {
        final JSONArray options = new JSONArray();

        JSONObject obj = new JSONObject();

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_ANY"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_ANY"));
        obj.put("default", true);
        obj.put("technology", "");

        //Filter for 3G + 4G
        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_3G_4G"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_3G_4G"));
        obj.put("technology", "34");

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_2G"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_2G"));
        obj.put("technology", "2");

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_3G"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_3G"));
        obj.put("technology", "3");

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_4G"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_4G"));
        obj.put("technology", "4");

        obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_5G"));
        obj.put("summary", labels.getString("MAP_FILTER_TECHNOLOGY_5G"));
        obj.put("technology", "5");


        final JSONObject result = new JSONObject();

        result.put("title", labels.getString("MAP_FILTER_TECHNOLOGY"));
        result.put("type", "MFT_TECHNOLOGY");
        result.put("options", options);

        return result;
    }

    private JSONObject getOperators(final boolean mobile) throws JSONException, SQLException {

        final JSONArray options = new JSONArray();
        String sql = null;

        JSONObject obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_ALL_OPERATORS"));
        obj.put("summary", "");
        obj.put("default", true);
        if (mobile) {
            obj.put("operator", "");
            sql = SQL_SELECT_PROVIDER_WITH_MCC_MCC;
        } else {
            obj.put("provider", "");
            sql = SQL_SELECT_PROVIDER_WITHOUT_MCC_MCC;
        }

        final PreparedStatement ps = conn.prepareStatement(sql);

        final ResultSet rs = ps.executeQuery();
        if (rs == null)
            return null;

        while (rs.next()) {
            final JSONObject obj2 = new JSONObject();
            options.put(obj2);
            obj2.put("title", rs.getString("shortname"));
            obj2.put("summary", rs.getString("name"));
            if (mobile)
                obj2.put("operator", rs.getLong("uid"));
            else
                obj2.put("provider", rs.getLong("uid"));
        }

        // close result set
        SQLHelper.closeResultSet(rs);

        // close prepared statement
        SQLHelper.closePreparedStatement(ps);

//        if (mobile)
//        {
//            obj = new JSONObject();
//            options.put(obj);
//            obj.put("title", labels.getString("MAP_FILTER_OTHER"));
//            obj.put("summary", "");
//            obj.put("operator", "other");
//        }

        final JSONObject result = new JSONObject();

        result.put("title", labels.getString("MAP_FILTER_CARRIER"));
        result.put("type", "MFT_OPERATOR");
        result.put("options", options);

        return result;
    }

    private JSONObject getDevices(final String type) throws JSONException, SQLException {

        final JSONArray options = new JSONArray();

        final JSONObject obj = new JSONObject();
        options.put(obj);
        obj.put("title", labels.getString("MAP_FILTER_ALL_DEVICES"));
        obj.put("summary", "");
        obj.put("device", "");
        obj.put("default", true);

        String typeFilter = "";
        if (type != null)
            if ("mobile".equals(type))
                typeFilter = " AND network_type not in (0, 97, 98, 99)";
            else if ("wifi".equals(type))
                typeFilter = " AND network_type = 99";
            else if ("browser".equals(type))
                typeFilter = " AND network_type = 98";

        final PreparedStatement ps = conn
                .prepareStatement(String
                        .format(SQL_SELECT_DEVICE_INFO,
                                typeFilter));

        final ResultSet rs = ps.executeQuery();
        if (rs == null)
            return null;

        final String summary = labels.getString("MAP_FILTER_DEVICE_SUMMARY");
        while (rs.next()) {
            final JSONObject obj2 = new JSONObject();
            options.put(obj2);
            final String modelValue = rs.getString("val");
            obj2.put("title", modelValue);
            obj2.put("summary", String.format("%s %s", summary, modelValue));
            obj2.put("device", rs.getString("keys"));
        }

        // close result set
        SQLHelper.closeResultSet(rs);

        // close prepared statement
        SQLHelper.closePreparedStatement(ps);

        final JSONObject result = new JSONObject();

        result.put("title", labels.getString("MAP_FILTER_DEVICE"));
        result.put("type", "MFT_DEVICE");
        result.put("options", options);

        return result;
    }

    @Post("json")
    @Get("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;

        final JSONObject answer = new JSONObject();

        // try parse the string to a JSON object
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
                    countries = ResourceManager.getCountries(new Locale(lang));
                }
            }

            final JSONObject mapFilterObject = new JSONObject();

            mapFilterObject.put("mapFilters", getMapFilterList());
            mapFilterObject.put("mapTypes", getMapTypeList());
            //logger.debug(getMapFilterList().toString());


            if( showCountriesInMapFilter) {
                answer.put("mapCountries", getCountries(entity));
            }
            answer.put("mapfilter", mapFilterObject);

            // log response
            logger.debug("rsponse: " + answer.toString());

            return answer.toString();
        } catch (final JSONException e) {
            logger.error(e.getMessage());
        } catch (final SQLException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
