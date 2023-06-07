/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.alladin.rmbt.statisticServer;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.cache.CacheHelper;
import at.alladin.rmbt.shared.cache.CacheHelper.ObjectWithTimestamp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(StatisticsResource.class);

    private static final int CACHE_STALE = 3600;
    private static final int CACHE_EXPIRE = 7200;

    private final CacheHelper cache = CacheHelper.getInstance();

    @Get
    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " + entity);

        addAllowOrigin();

        final StatisticParameters params = new StatisticParameters(settings.getString("RMBT_DEFAULT_LANGUAGE"), entity);

        final String cacheKey = CacheHelper.getHash(params);
        final ObjectWithTimestamp cacheObject = cache.getWithTimestamp(cacheKey, CACHE_STALE);
        if (cacheObject != null) {
            final String result = (String) cacheObject.o;
            logger.debug("cache hit");
            if (cacheObject.stale) {
                final Runnable refreshCacheRunnable = new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("adding in background: " + cacheKey);
                        final String result = generateStatistics(params, cacheKey);
                        if (result != null)
                            cache.set(cacheKey, CACHE_EXPIRE, result, true);
                    }
                };
                cache.getExecutor().execute(refreshCacheRunnable);
            }
            return result; // cache hit
        }
        logger.debug("not in cache");

        final String result = generateStatistics(params, cacheKey);
        if (result != null)
            cache.set(cacheKey, CACHE_EXPIRE, result, true);

        // log response
        logger.debug("rsponse: " + result);

        return result;
    }

    private String generateStatistics(final StatisticParameters params, final String cacheKey) {
        String result;
        final String lang = params.getLang();
        final float quantile = params.getQuantile();
        final int maxDevices = params.getMaxDevices();
        final String type = params.getType();
        final String networkTypeGroup = params.getNetworkTypeGroup();
        final double accuracy = params.getAccuracy();
        final String country = params.getCountry();
        final LocalDate startDate = params.getStartDate();
        final LocalDate endDate = params.getEndDate();
        final boolean useMobileProvider;
        final boolean signalMobile;
        final String where;
        if (type.equals("mobile")) {
            signalMobile = true;
            useMobileProvider = true;

            if (networkTypeGroup == null)
                where = "nt.type = 'MOBILE'";
            else {
                if ("2G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '2G'";
                else if ("3G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '3G'";
                else if ("4G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '4G'";
                else if ("5G".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name = '5G'";
//                else if ("NR".equalsIgnoreCase(networkTypeGroup))
//                    where = "nt.group_name = 'NR'";
                else if ("mixed".equalsIgnoreCase(networkTypeGroup))
                    where = "nt.group_name IN ('2G/3G','2G/4G','3G/4G','2G/3G/4G')";
                else
                    where = "1=0";
            }
        } else if (type.equals("wifi")) {
            where = "nt.type='WLAN'";
            signalMobile = false;
            useMobileProvider = false;
        } else if (type.equals("browser")) {
            where = "nt.type = 'LAN'";
            signalMobile = false;
            useMobileProvider = false;
        } else {   // invalid request
            where = "1=0";
            signalMobile = false;
            useMobileProvider = false;
        }

        final ExecutorService ex = Executors.newFixedThreadPool(7);
        final JSONArray empty = new JSONArray();
        final JSONObject answer = new JSONObject();

        final CompletableFuture<JSONArray> providers = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement ps = selectProviders(connection, true, quantile, accuracy, country, useMobileProvider, where, signalMobile, startDate, endDate);
                 ResultSet rs = ps.executeQuery()) {
                final JSONArray arr = new JSONArray();
                fillJSON(lang, rs, arr);
                return arr;
            } catch (final JSONException | SQLException | NamingException e) {
                logger.error(e.getMessage());
                return empty;
            }
        }, ex);

        final CompletableFuture<JSONArray> providersAll = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement ps = selectProvidersAllSection(connection, true, quantile, accuracy, country, useMobileProvider, where, signalMobile, startDate, endDate);
                 ResultSet rs = ps.executeQuery()) {
                final JSONArray arr = new JSONArray();
                fillJSON(lang, rs, arr);
                return arr;
            } catch (final JSONException | SQLException | NamingException e) {
                logger.error(e.getMessage());
                return empty;
            }
        }, ex);

        final CompletableFuture<JSONArray> devices = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement ps = selectDevices(connection, true, quantile, accuracy, country, useMobileProvider, where, maxDevices, startDate, endDate);
                 ResultSet rs = ps.executeQuery()) {
                final JSONArray arr = new JSONArray();
                fillJSON(lang, rs, arr);
                return arr;
            } catch (final JSONException | SQLException | NamingException e) {
                logger.error(e.getMessage());
                return empty;
            }
        }, ex);

        final CompletableFuture<JSONArray> devicesAll = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DbConnection.getConnection();
                 PreparedStatement ps = selectDevicesAllSection(connection, true, quantile, accuracy, country, useMobileProvider, where, maxDevices, startDate, endDate);
                 ResultSet rs = ps.executeQuery()) {
                final JSONArray arr = new JSONArray();
                fillJSON(lang, rs, arr);
                return arr;
            } catch (final JSONException | SQLException | NamingException e) {
                logger.error(e.getMessage());
                return empty;
            }
        }, ex);

        final CompletableFuture<JSONArray> countries = CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DbConnection.getConnection()) {
                return new JSONArray(getCountries(connection));
            } catch (final JSONException | SQLException | NamingException e) {
                logger.error(e.getMessage());
                return empty;
            }
        }, ex);

        CompletableFuture.allOf(providers, providersAll, devices, devicesAll, countries).join();
        answer.put("providers", providers.getNow(empty));
        answer.put("providersAll", providersAll.getNow(empty));
        answer.put("devices", devices.getNow(empty));
        answer.put("devicesAll", devicesAll.getNow(empty));
        answer.put("countries", countries.getNow(empty));

        ex.shutdown();

        answer.put("quantile", quantile);
        answer.put("startDate", startDate);
        answer.put("endDate", endDate);
        answer.put("type", type);

        // NT-562 - Improve performance of displaying statistics
        // not used in current implementation
//            try (PreparedStatement ps = selectProviders(conn, false, quantile, durationDays, accuracy, country, useMobileProvider, where, signalMobile);
//                 ResultSet rs = ps.executeQuery()) {
//                final JSONArray providersSumsArray = new JSONArray();
//                fillJSON(lang, rs, providersSumsArray);
//                if (providersSumsArray.length() == 1)
//                    answer.put("providers_sums", providersSumsArray.get(0));
//            }

        // NT-562 - Improve performance of displaying statistics
        // not used in current implementation
//            try (PreparedStatement ps = selectDevices(conn, false, quantile, durationDays, accuracy, country, useMobileProvider, where, maxDevices);
//                 ResultSet rs = ps.executeQuery()) {
//                final JSONArray devicesSumsArray = new JSONArray();
//                fillJSON(lang, rs, devicesSumsArray);
//                if (devicesSumsArray.length() == 1)
//                    answer.put("devices_sums", devicesSumsArray.get(0));
//            }

            // NT-562 - Improve performance of displaying statistics
            // not used in current implementation, will see what will happen :)
//            final JSONArray countries = new JSONArray(getCountries(conn));
//            answer.put("countries", countries);

        result = answer.toString();

        // log response
        logger.debug(result);

        return result;
    }

    private static Set<String> getCountries(Connection conn) throws SQLException {
        Set<String> countries = new TreeSet<>();
        String sql = "WITH RECURSIVE t(n) AS ( "
                + "SELECT MIN(mobile_network_id) FROM test"
                + " UNION"
                + " SELECT (SELECT mobile_network_id FROM test WHERE mobile_network_id > n"
                + " ORDER BY mobile_network_id LIMIT 1)"
                + " FROM t WHERE n IS NOT NULL"
                + " )"
                + "SELECT upper(mccmnc2name.country) FROM t LEFT JOIN mccmnc2name ON n=mccmnc2name.uid WHERE NOT mccmnc2name.country IS NULL GROUP BY mccmnc2name.country;";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                countries.add(rs.getString(1));
            return countries;
        }
    }

    private PreparedStatement selectProvidersAllSection(final Connection conn, final boolean group, final float quantile,
                                                        final double accuracy, final String country,
                                                        final boolean useMobileProvider, final String where,
                                                        final boolean signalMobile, LocalDate startDate, LocalDate endDate) throws SQLException {
        final PreparedStatement ps;
        final double THRESHOLD = .0001;
        final String percentile =
                " percentile_cont(?) within group (order by speed_download::bigint asc) quantile_down," +
                        " percentile_cont(?) within group (order by speed_upload::bigint asc) quantile_up," +
                        " percentile_cont(?) within group (order by signal_strength::bigint asc) quantile_signal," +
                        " percentile_cont(?) within group (order by coalesce(ping_shortest, ping_median)::bigint asc) quantile_ping";
        final String average =
                " AVG(speed_download::bigint) quantile_down," +
                        " AVG(speed_upload::bigint) quantile_up," +
                        " AVG(signal_strength::bigint) quantile_signal," +
                        " AVG(coalesce(ping_shortest, ping_median)::bigint) quantile_ping";
        final boolean isAverage = Math.abs(quantile + 1.0) < THRESHOLD;

        boolean isMobile = country != null && useMobileProvider;

        String sql = String
                .format("SELECT" +
                                " count(t.uid) count," +
                                (isAverage ? average : percentile) +
                                " FROM test t" +
                                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                                " JOIN provider p ON" +
                                (useMobileProvider ? " t.mobile_provider_id = p.uid" : " t.provider_id = p.uid") +
                                " WHERE %s" +
                                (isMobile ? " AND t.network_sim_country = ?" : "") +
//                                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                                " AND t.deleted = false AND t.status = 'FINISHED'" +
                                " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                                " AND time > ? " +
                                " AND time < ? "
                        , where);

        if (country != null) {
            sql = String.format("SELECT" +
                            " count(t.uid) count," +
                            (isAverage ? average : percentile) +
                            " FROM test t" +
                            " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                            // Reverted by K.G. July 15, 2019 - to enable grouping of mobile operators by MCC_MNC
                            (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                            //(useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.mccmnc = t.network_sim_operator" : "") +
                            " WHERE %s" +
                            " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT COALESCE(t.roaming_type, 0) = 2))" : "COALESCE(t.country_location, t.country_asn) = ? ") +
//                                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                                    " AND t.deleted = false AND t.status = 'FINISHED'" +
                            " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                            " AND time > ? " +
                            " AND time < ? "
                    , where);
        }

        ps = conn.prepareStatement(sql);

        int i = 1;

        if (!isAverage) {
            for (int j = 0; j < 3; j++) {
                ps.setFloat(i++, quantile);
            }

            ps.setFloat(i++, 1 - quantile); // inverse for ping
        }

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        ps.setDate(i++, Date.valueOf(startDate));

        ps.setDate(i++, Date.valueOf(endDate));

        logger.debug(ps.toString());

        return ps;
    }

    private PreparedStatement selectProviders(final Connection conn, final boolean group, final float quantile, final double accuracy,
                                              final String country, final boolean useMobileProvider, final String where, final boolean signalMobile, LocalDate startDate, LocalDate endDate) throws SQLException {
        final PreparedStatement ps;

        final double THRESHOLD = .0001;
        final String percentile =
                " percentile_cont(?) within group (order by speed_download::bigint asc) quantile_down," +
                        " percentile_cont(?) within group (order by speed_upload::bigint asc) quantile_up," +
                        " percentile_cont(?) within group (order by signal_strength::bigint asc) quantile_signal," +
                        " percentile_cont(?) within group (order by coalesce(ping_shortest, ping_median)::bigint asc) quantile_ping,";
        final String average =
                " AVG(speed_download::bigint) quantile_down," +
                        " AVG(speed_upload::bigint) quantile_up," +
                        " AVG(signal_strength::bigint) quantile_signal," +
                        " AVG(coalesce(ping_shortest, ping_median)::bigint) quantile_ping,";
        final boolean isAverage = Math.abs(quantile + 1.0) < THRESHOLD;
        final boolean isMobile = country != null && useMobileProvider;
        String sql = String
                .format("SELECT" +
                                (group ? " p.name, p.shortname, " : "") +
                                " count(t.uid) count," +
                                (isAverage ? average : percentile) +
                                " sum((speed_download >= ?)::int)::double precision / count(speed_download) down_green," +
                                " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(speed_download) down_yellow," +
                                " sum((speed_download < ?)::int)::double precision / count(speed_download) down_red," +

                                " sum((speed_upload >= ?)::int)::double precision / count(speed_upload) up_green," +
                                " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(speed_upload) up_yellow," +
                                " sum((speed_upload < ?)::int)::double precision / count(speed_upload) up_red," +

                                " sum((signal_strength >= ?)::int)::double precision / count(signal_strength) signal_green," +
                                " sum((signal_strength < ? and signal_strength >= ?)::int)::double precision / count(signal_strength) signal_yellow," +
                                " sum((signal_strength < ?)::int)::double precision / count(signal_strength) signal_red," +

                                " sum((coalesce(ping_shortest, ping_median) <= ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_green," +
                                " sum((coalesce(ping_shortest, ping_median) > ? and ping_shortest <= ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_yellow," +
                                " sum((coalesce(ping_shortest, ping_median) > ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_red" +

                                " FROM test t" +
                                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                                " JOIN provider p ON" +
                                (useMobileProvider ? " t.mobile_provider_id = p.uid" : " t.provider_id = p.uid") +
                                " WHERE %s" +
                                (isMobile ? " AND t.network_sim_country = ?" : "") +
//                                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                                " AND t.deleted = false AND t.status = 'FINISHED'" +
                                " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                                " AND time > ? " +
                                " AND time < ? " +
                                (group ? " GROUP BY p.uid" : "") +
                                " ORDER BY count DESC"
                        , where);

        if (country != null) {
            sql = String
                    .format("SELECT" +
                                    ((group && useMobileProvider) ? " p.name AS name, p.shortname AS shortname,  p.mccmnc AS sim_mcc_mnc, " : "") +
//                                    ((group && !useMobileProvider) ? " public_ip_as_name AS name, public_ip_as_name AS shortname, t.public_ip_asn AS asn,  " : "") +
                                    ((group && !useMobileProvider) ? " public_ip_as_name AS name, public_ip_as_name AS shortname,  " : "") +
                                    " count(t.uid) count," +
                                    (isAverage ? average : percentile) +
                                    " sum((speed_download >= ?)::int)::double precision / count(speed_download) down_green," +
                                    " sum((speed_download < ? and speed_download >= ?)::int)::double precision / count(speed_download) down_yellow," +
                                    " sum((speed_download < ?)::int)::double precision / count(speed_download) down_red," +

                                    " sum((speed_upload >= ?)::int)::double precision / count(speed_upload) up_green," +
                                    " sum((speed_upload < ? and speed_upload >= ?)::int)::double precision / count(speed_upload) up_yellow," +
                                    " sum((speed_upload < ?)::int)::double precision / count(speed_upload) up_red," +

                                    " sum((signal_strength >= ?)::int)::double precision / count(signal_strength) signal_green," +
                                    " sum((signal_strength < ? and signal_strength >= ?)::int)::double precision / count(signal_strength) signal_yellow," +
                                    " sum((signal_strength < ?)::int)::double precision / count(signal_strength) signal_red," +

                                    " sum((coalesce(ping_shortest, ping_median) <= ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_green," +
                                    " sum((coalesce(ping_shortest, ping_median) > ? and ping_shortest <= ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_yellow," +
                                    " sum((coalesce(ping_shortest, ping_median) > ?)::int)::double precision / count(coalesce(ping_shortest, ping_median)) ping_red" +

                                    " FROM test t" +
                                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                                    // Reverted by K.G. July 15, 2019 - to enable grouping of mobile operators by MCC_MNC
                                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                                    //(useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.mccmnc = t.network_sim_operator" : "") +
                                    " WHERE %s" +
                                    " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT COALESCE(t.roaming_type, 0) = 2))" : "COALESCE(t.country_location, t.country_asn) = ? ") +
//                                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                                    " AND t.deleted = false AND t.status = 'FINISHED'" +
                                    " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                                    " AND time > ? " +
                                    " AND time < ? " +
                                    ((group && (useMobileProvider)) ? " GROUP BY p.uid, p.mccmnc" : "") +
//                                    ((group && (!useMobileProvider)) ? " GROUP BY t.public_ip_as_name, t.public_ip_asn" : "") +
                                    ((group && (!useMobileProvider)) ? " GROUP BY t.public_ip_as_name" : "") +
                                    " ORDER BY count DESC"
                            , where);
        }

        ps = conn.prepareStatement(sql);

        int i = 1;

        if (!isAverage) {
            for (int j = 0; j < 3; j++)
                ps.setFloat(i++, quantile);
            ps.setFloat(i++, 1 - quantile); // inverse for ping
        }


        final int[] td = classification.THRESHOLD_DOWNLOAD;
        ps.setInt(i++, td[0]);
        ps.setInt(i++, td[0]);
        ps.setInt(i++, td[1]);
        ps.setInt(i++, td[1]);

        final int[] tu = classification.THRESHOLD_UPLOAD;
        ps.setInt(i++, tu[0]);
        ps.setInt(i++, tu[0]);
        ps.setInt(i++, tu[1]);
        ps.setInt(i++, tu[1]);

        final int[] ts = signalMobile ? classification.THRESHOLD_SIGNAL_MOBILE : classification.THRESHOLD_SIGNAL_WIFI;
        ps.setInt(i++, ts[0]);
        ps.setInt(i++, ts[0]);
        ps.setInt(i++, ts[1]);
        ps.setInt(i++, ts[1]);

        final int[] tp = classification.THRESHOLD_PING;
        ps.setInt(i++, tp[0]);
        ps.setInt(i++, tp[0]);
        ps.setInt(i++, tp[1]);
        ps.setInt(i++, tp[1]);

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        ps.setDate(i++, Date.valueOf(startDate));
        ps.setDate(i++, Date.valueOf(endDate));


        logger.debug(ps.toString());

        return ps;
    }

    private static PreparedStatement selectDevicesAllSection(final Connection conn, final boolean group, final float quantile, final double accuracy,
                                                             final String country, final boolean useMobileProvider, final String where, final int maxDevices, LocalDate startDate, LocalDate endDate) throws SQLException {
        PreparedStatement ps;
        final double THRESHOLD = .0001;
        final String percentile =
                " percentile_cont(?) within group (order by speed_download::bigint asc) quantile_down," +
                        " percentile_cont(?) within group (order by speed_upload::bigint asc) quantile_up," +
                        " percentile_cont(?) within group (order by coalesce(ping_shortest, ping_median)::bigint asc) quantile_ping";
        final String average =
                " AVG(speed_download::bigint) quantile_down," +
                        " AVG(speed_upload::bigint) quantile_up," +
                        " AVG(coalesce(ping_shortest, ping_median)::bigint) quantile_ping";
        final boolean isAverage = Math.abs(quantile + 1.0) < THRESHOLD;

        String sql = String.format("SELECT" +
                " count(t.uid) count," +
                (isAverage ? average : percentile) +
                " FROM test t" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " WHERE %s" +
//                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                " AND t.deleted = false AND t.status = 'FINISHED'" +
                " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                " AND time > ? " +
                " AND time < ? " +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                " LIMIT %d", where, maxDevices);
        if (country != null) {
            sql = String.format("SELECT" +
                    " count(t.uid) count," +
                    (isAverage ? average : percentile) +
                    " FROM test t" +
                    " LEFT JOIN device_map adm ON adm.codename=t.model" +
                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                    // Reverted by K.G. July 15, 2019 - to enable grouping of mobile operators by MCC_MNC
                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                    //(useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.mccmnc = t.network_sim_operator" : "") +
                    " WHERE %s" +
//                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                    " AND t.deleted = false AND t.status = 'FINISHED'" +
                    " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                    " AND time > ? " +
                    " AND time < ? " +
                    " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT COALESCE(t.roaming_type, 0) = 2))" : "COALESCE(t.country_location, t.country_asn) = ? ") +
                    " LIMIT %d", where, maxDevices);
        }

        ps = conn.prepareStatement(sql);

        int i = 1;

        if (!isAverage) {
            for (int j = 0; j < 2; j++)
                ps.setFloat(i++, quantile);
            ps.setFloat(i++, 1 - quantile); // inverse for ping
        }

        ps.setDate(i++, Date.valueOf(startDate));
        ps.setDate(i++, Date.valueOf(endDate));

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        logger.debug(ps.toString());

        return ps;
    }

    private static PreparedStatement selectDevices(final Connection conn, final boolean group, final float quantile, final double accuracy,
                                                   final String country, final boolean useMobileProvider, final String where, final int maxDevices, LocalDate startDate, LocalDate endDate) throws SQLException {
        PreparedStatement ps;
        final double THRESHOLD = .0001;
        final String percentile =
                " percentile_cont(?) within group (order by speed_download::bigint asc) quantile_down," +
                        " percentile_cont(?) within group (order by speed_upload::bigint asc) quantile_up," +
                        " percentile_cont(?) within group (order by coalesce(ping_shortest, ping_median)::bigint asc) quantile_ping";
        final String average =
                " AVG(speed_download::bigint) quantile_down," +
                        " AVG(speed_upload::bigint) quantile_up," +
                        " AVG(coalesce(ping_shortest, ping_median)::bigint) quantile_ping";
        final boolean isAverage = Math.abs(quantile + 1.0) < THRESHOLD;

        String sql = String.format("SELECT" +
                (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                " count(t.uid) count," +
                (isAverage ? average : percentile) +
                " FROM test t" +
                " LEFT JOIN device_map adm ON adm.codename=t.model" +
                " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                " WHERE %s" +
//                " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                " AND t.deleted = false AND t.status = 'FINISHED'" +
                " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                " AND time > ? " +
                " AND time < ? " +
                (useMobileProvider ? " AND t.mobile_provider_id IS NOT NULL" : "") +
                (group ? " GROUP BY COALESCE(adm.fullname, t.model) " : "") +
                " ORDER BY count DESC" +
                " LIMIT %d", where, maxDevices);
        if (country != null) {
            sql = String.format("SELECT" +
                    (group ? " COALESCE(adm.fullname, t.model) model," : "") +
                    " count(t.uid) count," +
                    (isAverage ? average : percentile) +
                    " FROM test t" +
                    " LEFT JOIN device_map adm ON adm.codename=t.model" +
                    " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
                    // Reverted by K.G. July 15, 2019 - to enable grouping of mobile operators by MCC_MNC
                    (useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.uid = t.mobile_sim_id" : "") +
                    //(useMobileProvider ? " LEFT JOIN mccmnc2name p ON p.mccmnc = t.network_sim_operator" : "") +
                    " WHERE %s" +
//                    " AND t.deleted = false AND t.implausible = false AND t.status = 'FINISHED'" +
//                    " AND t.deleted = false AND t.status = 'FINISHED'" +
                    " AND t.deleted = false  AND t.implausible = false AND t.zero_measurement = false AND t.status = 'FINISHED'" +
                    " AND time > ? " +
                    " AND time < ? " +
                    " AND " + (useMobileProvider ? "p.country = ? AND ((t.country_location IS NULL OR t.country_location = ?)  AND (NOT COALESCE(t.roaming_type, 0) = 2))" : "COALESCE(t.country_location, t.country_asn) = ? ") +
                    (group ? " GROUP BY COALESCE(adm.fullname, t.model) " : "") +
                    " ORDER BY count DESC" +
                    " LIMIT %d", where, maxDevices);
        }

        ps = conn.prepareStatement(sql);

        int i = 1;

        if (!isAverage) {
            for (int j = 0; j < 2; j++)
                ps.setFloat(i++, quantile);
            ps.setFloat(i++, 1 - quantile); // inverse for ping
        }

        ps.setDate(i++, Date.valueOf(startDate));
        ps.setDate(i++, Date.valueOf(endDate));

        if (country != null) {
            if (useMobileProvider) {
                ps.setString(i++, country.toLowerCase()); //mccmnc2name.country
                ps.setString(i++, country.toUpperCase()); //country_location
            } else {
                ps.setString(i++, country.toUpperCase());
            }
        }

        logger.debug(ps.toString());

        return ps;
    }

    private static void fillJSON(final String lang, final ResultSet rs, final JSONArray providers)
            throws SQLException, JSONException {
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            final JSONObject obj = new JSONObject();
            for (int j = 1; j <= columnCount; j++) {
                final String colName = metaData.getColumnName(j);
                Object data = rs.getObject(j);
                if (colName.equals("name") && data == null)
                    try {
                        // try to get asn and providers name
                        if (rs.getObject(j + 2) != null && rs.getObject(j + 2) instanceof Long) {
                            long asn = rs.getLong(j + 2);
                            data = Helperfunctions.getASName(asn);
                        } else {
                            if (lang != null && lang.equals("de")) {
                                data = "Andere Betreiber";
                            } else {
                                data = "Other operators";
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                if (colName.equals("shortname") && data == null) {
                    try {
                        // try to get asn and providers name
                        if (rs.getObject(j + 1) != null && rs.getObject(j + 1) instanceof Long) {
                            long asn = rs.getLong(j + 1);
                            data = Helperfunctions.getASName(asn);
                        } else {
                            if (lang != null && lang.equals("de")) {
                                data = "Andere";
                            } else {
                                data = "Others";
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage());
                    }
                }
                obj.put(colName, data);
            }
            providers.put(obj);
        }
    }
}