/******************************************************************************
 Copyright 2013-2015 alladin-IT GmbH

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 */
package at.alladin.rmbt.controlServer.OffOnNet.service;

import at.alladin.rmbt.controlServer.ServerResource;
import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.Settings;
import at.alladin.rmbt.shared.json.TestServerJson;
import at.alladin.rmbt.shared.json.TestServerJsonInterface;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class OffOnNetServerResource extends ServerResource implements Settings {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(OffOnNetServerResource.class);

    private static final String SQL_QOS_SERVER_BY_USER_ON_NET =
            "SELECT ts1.*\n" +
                    "FROM test_server ts1, test_servers_providers tsp\n" +
                    "WHERE server_group in (\n" +
                    "    SELECT ts.server_group\n" +
                    "    FROM test_server ts\n" +
                    "        JOIN test t ON (ts.uid = t.server_id)\n" +
                    "        JOIN client c ON (c.uid = t.client_id)\n" +
                    "    WHERE c.uuid = ?\n" +
                    "    ORDER BY t.uid DESC LIMIT 1\n" +
                    ") AND  ts1.server_type = 'QoS' AND tsp.test_server_id = ts1.uid AND tsp.provider_id = ?";

    private static final String SQL_QOS_SERVER_BY_USER_OFF_NET =
            "SELECT * FROM test_server WHERE server_group in "
                    + "( SELECT ts.server_group FROM test_server ts JOIN test t ON (ts.uid = t.server_id) "
                    + "JOIN client c ON (c.uid = t.client_id) WHERE c.uuid = ? ORDER BY t.uid DESC LIMIT 1 ) "
                    + "AND  server_type = 'QoS'";

    private static final String SQL_TEST_SERVER_WITH_GPS =
            "SELECT st_distance(st_geographyfromtext('POINT('||?||' '||?||')'), st_geographyfromtext('POINT('||geo_long||' '|| geo_lat||')')) / 1000 as distance, *"
                    + " FROM test_server"
                    + " WHERE active AND server_type = ? AND (country = ? OR country = 'any' OR country IS NULL)"
                    + " ORDER BY distance ASC, (country != 'any' AND country IS NOT NULL) DESC, name ASC"
                    + " LIMIT 10";

    public static final String SQL_TEST_SERVER_WITH_GPS_ON_NET =
            "SELECT st_distance(\n" +
                    "    st_geographyfromtext('POINT('||?||' '||?||')'),\n" +
                    "    st_geographyfromtext('POINT('||geo_long||' '|| geo_lat||')')\n" +
                    ") / 1000 as distance, *\n" +
                    "FROM test_server, test_servers_providers tsp\n" +
                    "WHERE active \n" +
                    "  AND server_type = ? \n" +
                    "  AND (country = ? OR country = 'any' OR country IS NULL) \n" +
                    "  AND tsp.test_server_id = test_server.uid \n" +
                    "  AND tsp.provider_id = ?\n" +
                    "ORDER BY distance ASC, (country != 'any' AND country IS NOT NULL) DESC, name ASC\n" +
                    "LIMIT 10";

    private static final String SQL_TEST_SERVER_WITHOUT_GPS =
            "SELECT * FROM test_server" + " WHERE active" + " AND server_type = ?"
                    + " AND (country = ? OR country = 'any' OR country IS NULL)" + " ORDER BY"
                    + " (country != 'any' AND country IS NOT NULL) DESC, name ASC LIMIT 10";



    private static final String SQL_ID_PROVIDER_BY_USER_PORT =
            "select provider_id\n" +
            "from client_port_provider\n" +
            "where client_uuid = ? AND port = ?" ;

    @Override
    public List<TestServerJsonInterface> getNearestServer(
            Double geolat,
            Double geolong,
            String clientIp,
            String asCountry,
            String geoIpCountry,
            String serverType,
            boolean ssl,
            Boolean ipv6
    ) {
        return this.getNearestServer(
                geolat,
                geolong,
                clientIp,
                asCountry,
                geoIpCountry,
                serverType,
                ssl,
                ipv6,
                false,
                0
        );
    }
    public List<TestServerJsonInterface> getNearestServer(
            Double geoLat,
            Double geoLng,
            String clientIp,
            String asCountry,
            String geoIpCountry,
            String serverType,
            boolean ssl,
            Boolean ipv6,
            boolean onNet,
            int providerId
    ) {
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

            if (!(geoLat == Double.MAX_VALUE && geoLng == Double.MAX_VALUE)) {
                // We will find by geo location
                ps = conn.prepareStatement(onNet ? SQL_TEST_SERVER_WITH_GPS_ON_NET : SQL_TEST_SERVER_WITH_GPS );
                ps.setDouble(i++, geoLng);
                ps.setDouble(i++, geoLat);
                ps.setString(i++, serverType);
                ps.setString(i++, country.toLowerCase());
                if (onNet) {
                    ps.setInt(i, providerId);
                }
            } else {
                ps = conn.prepareStatement(SQL_TEST_SERVER_WITHOUT_GPS);
                ps.setString(i++, serverType);
                ps.setString(i, country.toLowerCase());
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



    @Override
    public TestServerJson getQoSServerByUserID(
            UUID client_uuid,
            boolean ssl,
            Boolean ipv6
    ) {
        return this.getQoSServerByUserID(client_uuid, ssl, ipv6, false, 0);
    }
    public TestServerJson getQoSServerByUserID(
            UUID client_uuid,
            boolean ssl,
            Boolean ipv6,
            boolean onNet,
            int port
    ) {
        String address;
        TestServerJson testServer = null;
        PreparedStatement ps;


        try {
            if (onNet && port > 0) {
                ps = conn.prepareStatement(SQL_QOS_SERVER_BY_USER_ON_NET);
                int provider = this.getProviderIdByUserPort(client_uuid, port);
                if (provider == 0) {
                    throw new Error("provider was not found by port: " + port + " for user " + client_uuid);
                }
                ps.setObject(1, client_uuid);
                ps.setObject(2, provider);
            } else {
                ps = conn.prepareStatement(SQL_QOS_SERVER_BY_USER_OFF_NET);
                ps.setObject(1, client_uuid);
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

                    testServer = new TestServerJson(
                            rs.getInt("uid"),
                            rs.getString("name") + " (" + rs.getString("city") + ")",
                            rs.getInt(ssl ? "port_ssl" : "port"),
                            address
                    );
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

    public int getProviderIdByUserPort(UUID uuid, int port) {
        try {
            PreparedStatement ps = conn.prepareStatement(SQL_ID_PROVIDER_BY_USER_PORT);
            ps.setObject(1, uuid);
            ps.setObject(2, port);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int provider = rs.getInt("provider_id");
                SQLHelper.closeResultSet(rs);
                SQLHelper.closePreparedStatement(ps);
                return provider;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return 0;
        }
    }
}
