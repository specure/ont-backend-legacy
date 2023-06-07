/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.*;
import at.alladin.rmbt.shared.json.TestServerJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
import at.alladin.rmbt.shared.reporting.AdvancedReporting;
import com.google.common.base.Strings;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ServerResource extends org.restlet.resource.ServerResource implements Settings {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ServerResource.class);

    private static final String SQL_TEST_SERVER_WITH_GPS =
            "SELECT st_distance(st_geographyfromtext('POINT('||?||' '||?||')'), st_geographyfromtext('POINT('||geo_long||' '|| geo_lat||')')) / 1000 as distance, *"
                    + " FROM test_server"
                    + " WHERE active AND server_type = ? AND (country = ? OR country = 'any' OR country IS NULL)"
                    + " ORDER BY distance ASC, (country != 'any' AND country IS NOT NULL) DESC, name ASC"
                    + " LIMIT 10";

    private static final String SQL_TEST_SERVER_WITHOUT_GPS =
            "SELECT * FROM test_server" + " WHERE active" + " AND server_type = ?"
                    + " AND (country = ? OR country = 'any' OR country IS NULL)" + " ORDER BY"
                    + " (country != 'any' AND country IS NOT NULL) DESC, name ASC LIMIT 10";

    private static final String SQL_QOS_SERVER_BY_USER =
            "SELECT * FROM test_server WHERE server_group in "
                    + "( SELECT ts.server_group FROM test_server ts JOIN test t ON (ts.uid = t.server_id) "
                    + "JOIN client c ON (c.uid = t.client_id) WHERE c.uuid = ? ORDER BY t.uid DESC LIMIT 1 ) "
                    + "AND  server_type = 'QoS'";

    private static final String SQL_JPL_CONTROL = "Select count(uid) from test_jpl where test_uid = ?";

    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected Classification classification;
    protected JsonUtil jsonUtil;

    @Override
    public void doInit() throws ResourceException {
        super.doInit();

        classification = Classification.getInstance();

        String customer = "specure";
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            customer = (String) environmentContext.lookup("customer");
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }

        settings = ResourceManager.getCfgBundle(new Locale(customer));
        // Set default Language for System
        Locale.setDefault(new Locale(settings.getString("RMBT_DEFAULT_LANGUAGE")));
        labels = ResourceManager.getSysMsgBundle();

        jsonUtil = new JsonUtil(labels, false);

        // Get DB-Connection
        try {
            conn = DbConnection.getConnection();
        } catch (final NamingException e) {
            logger.error(e.getMessage());
        } catch (final SQLException e) {
            logger.error(labels.getString("ERROR_DB_CONNECTION_FAILED") + e.getMessage());
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        super.doRelease();

        // close connection
        SQLHelper.closeConnection(conn);
    }

    @SuppressWarnings("unchecked")
    protected void addAllowOrigin() {
        Series<Header> responseHeaders =
                (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.add("Access-Control-Allow-Credentials", "false");
        responseHeaders.add("Access-Control-Max-Age", "60");
    }

    @Options
    public void doOptions(final Representation entity) {
        addAllowOrigin();
    }

    @SuppressWarnings("unchecked")
    public String getIP() {
        final Series<Header> headers =
                (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realIp = headers.getFirstValue("X-Real-IP", true);
        if (realIp != null)
            return realIp;
        else
            return getRequest().getClientInfo().getAddress();
    }

    @SuppressWarnings("unchecked")
    public Reference getURL() {
        final Series<Header> headers =
                (Series<Header>) getRequest().getAttributes().get("org.restlet.http.headers");
        final String realURL = headers.getFirstValue("X-Real-URL", true);
        if (realURL != null)
            return new Reference(realURL);
        else
            return getRequest().getOriginalRef();
    }

    @Override
    public String getSetting(String key) {
        return getSetting(key, null);
    }

    // TODO: add caching!
    @Override
    public String getSetting(String key, String lang) {
        return SettingsHelper.getSetting(conn, key, lang);
    }

    public AdvancedReporting getAdvancedReporting()
            throws InstantiationException, IllegalAccessException {
        return AdvancedReporting.newInstance();
    }

    /**
     * @param geolat
     * @param geolong
     * @param clientIp
     * @param asCountry
     * @param geoIpCountry
     * @param serverType
     * @param ssl
     * @param ipv6
     * @return
     */
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

            if (country == null ) {
                country = "any";
            }

            if (!(geolat == Double.MAX_VALUE && geolong == Double.MAX_VALUE)) {
                // We will find by geo location
                ps = conn.prepareStatement(SQL_TEST_SERVER_WITH_GPS);
                ps.setDouble(i++, geolong);
                ps.setDouble(i++, geolat);
                ps.setString(i++, serverType);
                ps.setString(i++, country.toLowerCase());
            } else {
                ps = conn.prepareStatement(SQL_TEST_SERVER_WITHOUT_GPS);
                ps.setString(i++, serverType);
                ps.setString(i++, country.toLowerCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    if (ipv6 == null) {
                        address = rs.getString("web_address");
                    } else if (ipv6) {
                        address = rs.getString("web_address_ipv6");
                    } else {
                        address = rs.getString("web_address_ipv4");
                    }

                    testServers.add(new TestServerJson(rs.getInt("uid"),
                            rs.getString("name") + " (" + rs.getString("city") + ")",
                            rs.getInt(ssl ? "port_ssl" : "port"), address));
                }

                // close result set
                SQLHelper.closeResultSet(rs);

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);

                return testServers;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            // TODO, to think
            // close prepared statement
            SQLHelper.closePreparedStatement(ps);
        }

    }

    public TestServerJson getQoSServerByUserID(UUID client_uuid, boolean ssl, Boolean ipv6) {
        String address = null;
        TestServerJson testServer = null;
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(SQL_QOS_SERVER_BY_USER);
            ps.setObject(1, client_uuid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (ipv6 == null) {
                        address = rs.getString("web_address");
                    } else if (ipv6) {
                        address = rs.getString("web_address_ipv6");
                    } else {
                        address = rs.getString("web_address_ipv4");
                    }

                    testServer = new TestServerJson(rs.getInt("uid"),
                            rs.getString("name") + " (" + rs.getString("city") + ")",
                            rs.getInt(ssl ? "port_ssl" : "port"), address);
                }
                if (testServer == null) {
                    // try find nearly serve
                }

                // close result set
                SQLHelper.closeResultSet(rs);

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);

                return testServer;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * Method checks if exists record in test_jpl table
     *
     * @param test_uid
     * @return
     */
    protected boolean existJPLRecord(String test_uid) {

        boolean resultBoolean = false;

        // check test_uuid
        if (test_uid != null && test_uid.isEmpty() == false) {

            logger.debug("ExistJPL: " + test_uid);

            if (test_uid.isEmpty()) {
                return false;
            }

            try {
                PreparedStatement ps = conn.prepareStatement(SQL_JPL_CONTROL);
                ps.setObject(1, UUID.fromString(test_uid));

                ResultSet rs = ps.executeQuery();
                rs.next();

                long countRow = rs.getLong("count");
                if (countRow != 0) {
                    resultBoolean = true;
                }

                // close result set
                SQLHelper.closeResultSet(rs);

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);

            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

        return resultBoolean;
    }

    public static String getSqlTestServerWithGps(){
        return SQL_TEST_SERVER_WITH_GPS;
    }

    public static String getSqlTestServerWithoutGps(){
        return SQL_TEST_SERVER_WITHOUT_GPS;
    }

    public static String getSqlQosServerByUser(){
        return SQL_QOS_SERVER_BY_USER;
    }

    public static String getSqlJplControl(){
        return SQL_JPL_CONTROL;
    }
}
