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

import at.alladin.rmbt.shared.ResourceManager;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class InfoResourceV3 extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(InfoResourceV3.class);

    // show points on map( RATEL, AKOS)
    private final Boolean showPointsOnMap = CustomerResource.getInstance().showPointsOnMap();
    // show regions, municipalities, settlements, white spots on map( AKOS)
    private final Boolean showRegionsOnMap = CustomerResource.getInstance().showRegionsOnMap();
    private final boolean showSettlementsOnMap = CustomerResource.getInstance().showSettlementsOnMap();
    private final boolean showMunicipalitiesOnMap = CustomerResource.getInstance().showMunicipalitiesOnMap();
    private final boolean showWhiteSpotsOnMap = CustomerResource.getInstance().showWhiteSpotsOnMap();

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " + entity);

        addAllowOrigin();

        JSONObject request = null;

        final JSONObject answer = new JSONObject();

        // try parse the string to a JSON object
        try {

            String lang = settings.getString("RMBT_DEFAULT_LANGUAGE");

            if (entity != null) {
                request = new JSONObject(entity);

                lang = request.optString("language");

                // Load Language Files for Client
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    labels = null;
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                    countries = ResourceManager.getCountries(new Locale(lang));
                }
            }

            answer.put("mapTypes", getMapTypeList());
            answer.put("mapSubTypes", getMapSubTypeList());
            answer.put("mapCellularTypes", getMapCellularTypeList());
            answer.put("mapPeriodFilters", getMapPeriodFiltersList());
            //answer.put("mapLists", getMapListsList());
            answer.put("mapOverlays", getMapOverlaysList());
            answer.put("mapStatistics", getMapStatisticsList());
            answer.put("mapLayouts", getMapLayoutsList());

            // log response
            logger.debug("rsponse: " + answer.toString());

            return answer.toString();

        } catch (JSONException e) {
            logger.error(e.getMessage());
        }

        // log response
        logger.debug("rsponse: " + answer.toString());

        return null;
    }

    private JSONArray getMapTypeList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;
        JSONArray mapSubTypeOptions;

        // Mobile
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_MOBILE"));
        obj.put("id", "mobile");
        mapSubTypeOptions = new JSONArray();
        mapSubTypeOptions.put(0).put(1).put(2).put(3);
        obj.put("mapSubTypeOptions", mapSubTypeOptions);
        obj.put("mapCellularTypeOptions", true);
        obj.put("mapListOptions", 0);
        result.put(obj);

        // WiFi
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_WIFI"));
        obj.put("id", "wifi");
        mapSubTypeOptions = new JSONArray();
        mapSubTypeOptions.put(0).put(1).put(2).put(4);
        obj.put("mapSubTypeOptions", mapSubTypeOptions);
        obj.put("mapListOptions", 1);
        result.put(obj);

        // Browser
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_BROWSER"));
        obj.put("id", "browser");
        mapSubTypeOptions = new JSONArray();
        mapSubTypeOptions.put(0).put(1).put(2);
        obj.put("mapSubTypeOptions", mapSubTypeOptions);
        obj.put("mapListOptions", 1);
        result.put(obj);

        // All
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_ALL"));
        obj.put("id", "all");
        mapSubTypeOptions = new JSONArray();
        mapSubTypeOptions.put(0).put(1).put(2);
        obj.put("mapSubTypeOptions", mapSubTypeOptions);
        obj.put("default", true);
        obj.put("mapListOptions", 1);
        result.put(obj);

        return result;
    }

    private JSONArray getMapSubTypeList() throws JSONException {
        final JSONArray result = new JSONArray();

        JSONObject obj;
        JSONArray heatmapColors = new JSONArray();
        heatmapColors.put("#600000").put("#ff0000").put("#ffff00").put("#00ff00").put("#00cb00").put("#009600").put("#006100");

        JSONArray downloadHeatmapCaptions = new JSONArray();
        downloadHeatmapCaptions.put("0.2").put("").put("4.5").put("").put("111.8").put("").put(labels.getString("MAP_FILTER_DOWNLOAD_UNIT"));

        JSONArray uploadHeatmapCaptions = new JSONArray();
        uploadHeatmapCaptions.put("0.4").put("").put("1.4").put("").put("5.7").put("").put(labels.getString("MAP_FILTER_UPLOAD_UNIT"));

        JSONArray pingHeatmapCaptions = new JSONArray();
        pingHeatmapCaptions.put("500").put("").put("50").put("").put("5").put("").put(labels.getString("MAP_FILTER_PING_UNIT"));

        JSONArray cellularSignalHeatmapCaptions = new JSONArray();
        cellularSignalHeatmapCaptions.put("-108").put("").put("-94").put("").put("-78").put("").put(labels.getString("MAP_FILTER_SIGNAL_UNIT"));

        JSONArray wifiSignalHeatmapCaptions = new JSONArray();
        wifiSignalHeatmapCaptions.put("-99").put("").put("-69").put("").put("-39").put("").put(labels.getString("MAP_FILTER_SIGNAL_UNIT"));

        // Download
        obj = new JSONObject();
        obj.put("index", 0);
        obj.put("id", "download");
        obj.put("default", 1);
        obj.put("title", labels.getString("MAP_FILTER_DOWNLOAD"));
        obj.put("heatmap_colors", heatmapColors);
        obj.put("heatmap_captions", downloadHeatmapCaptions);
        result.put(obj);

        // Upload
        obj = new JSONObject();
        obj.put("index", 1);
        obj.put("id", "upload");
        obj.put("title", labels.getString("MAP_FILTER_UPLOAD"));
        obj.put("heatmap_colors", heatmapColors);
        obj.put("heatmap_captions", uploadHeatmapCaptions);
        result.put(obj);

        // Ping
        obj = new JSONObject();
        obj.put("index", 2);
        obj.put("id", "ping");
        obj.put("title", labels.getString("MAP_FILTER_PING"));
        obj.put("heatmap_colors", heatmapColors);
        obj.put("heatmap_captions", pingHeatmapCaptions);
        result.put(obj);

        // Cellular signal
        obj = new JSONObject();
        obj.put("index", 3);
        obj.put("id", "signal");
        obj.put("title", labels.getString("MAP_FILTER_SIGNAL"));
        obj.put("heatmap_colors", heatmapColors);
        obj.put("heatmap_captions", cellularSignalHeatmapCaptions);
        result.put(obj);

        // WiFi signal
        obj = new JSONObject();
        obj.put("index", 4);
        obj.put("id", "signal");
        obj.put("title", labels.getString("MAP_FILTER_SIGNAL"));
        obj.put("heatmap_colors", heatmapColors);
        obj.put("heatmap_captions", wifiSignalHeatmapCaptions);
        result.put(obj);

        return result;
    }

    private JSONArray getMapCellularTypeList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;

        // 2G
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_2G"));
        obj.put("id", 2);
        obj.put("default", true);
        result.put(obj);

        // 3G
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_3G"));
        obj.put("id", 3);
        obj.put("default", true);
        result.put(obj);

        // 4G
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_4G"));
        obj.put("id", 4);
        obj.put("default", true);
        result.put(obj);

        // 5G
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_TECHNOLOGY_5G"));
        obj.put("id", 5);
        obj.put("default", true);
        result.put(obj);

        return result;
    }

    private JSONArray getMapPeriodFiltersList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;

        // 1 week
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_7_DAYS"));
        obj.put("period", 7);
        result.put(obj);

        // 1 month
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_30_DAYS"));
        obj.put("period", 30);
        result.put(obj);

        // 3 months
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_90_DAYS"));
        obj.put("period", 90);
        result.put(obj);

        // 6 months
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_180_DAYS"));
        obj.put("default", true);
        obj.put("period", 180);
        result.put(obj);

        // 1 year
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_365_DAYS"));
        obj.put("period", 365);
        result.put(obj);

        // 2 years
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_PERIOD_730_DAYS"));
        obj.put("period", 730);
        result.put(obj);

        return result;
    }

    private JSONArray getMapListsList() throws JSONException {

        final JSONArray result = new JSONArray();

        return result;
    }

    private JSONArray getMapOverlaysList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;

        if (showPointsOnMap) {
            // Automatic
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_AUTOMATIC"));
            obj.put("value", "MAP_FILTER_AUTOMATIC");
            obj.put("default", true);
            result.put(obj);
        }

        // Heatmap
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_HEATMAP"));
        obj.put("value", "MAP_FILTER_HEATMAP");
        if(!showPointsOnMap) obj.put("default", true);
        result.put(obj);

        if (showPointsOnMap) {
            // Points
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_POINTS"));
            obj.put("value", "MAP_FILTER_POINTS");
            result.put(obj);
        }

        // Regions, municipalities, settlements, white spots, but only for AKOS
        if (showRegionsOnMap) {
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_REGIONS"));
            obj.put("value", "MAP_FILTER_REGIONS");
            result.put(obj);
        }

        if (showMunicipalitiesOnMap) {
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_MUNICIPALITIES"));
            obj.put("value", "MAP_FILTER_MUNICIPALITIES");
            result.put(obj);
        }

        if (showSettlementsOnMap) {
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_SETTLEMENTS"));
            obj.put("value", "MAP_FILTER_SETTLEMENTS");
            result.put(obj);
        }

        if (showWhiteSpotsOnMap) {
            obj = new JSONObject();
            obj.put("title", labels.getString("MAP_FILTER_WHITE_SPOTS"));
            obj.put("value", "MAP_FILTER_WHITE_SPOTS");
            result.put(obj);
        }

        return result;
    }

    private JSONArray getMapStatisticsList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;

        // 80% percentile
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD_1_TITLE"));
        obj.put("value", 0.8);
        result.put(obj);

        // Median
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD_2_TITLE"));
        obj.put("value", 0.5);
        obj.put("default", true);
        result.put(obj);

        // 20% percentile
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_STATISTICAL_METHOD_3_TITLE"));
        obj.put("value", 0.2);
        result.put(obj);

        return result;
    }

    private JSONArray getMapLayoutsList() throws JSONException {

        final JSONArray result = new JSONArray();

        JSONObject obj;

        // Light
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_LIGHT"));
        obj.put("default", true);
        obj.put("apiLink", settings.getString("LIGHT_API_LINK"));
        obj.put("accessToken", settings.getString("LIGHT_ACCESS_TOKEN"));
        obj.put("layer", settings.getString("LIGHT_LAYER"));
        result.put(obj);

        // Dark
        obj = new JSONObject();
        obj.put("title", labels.getString("MAP_FILTER_DARK"));
        obj.put("apiLink", settings.getString("DARK_API_LINK"));
        obj.put("accessToken", settings.getString("DARK_ACCESS_TOKEN"));
        obj.put("layer", settings.getString("DARK_LAYER"));
        result.put(obj);

        return result;
    }

}
