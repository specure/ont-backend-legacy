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
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.GeoIPHelper;
import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.Classification;
import at.alladin.rmbt.shared.RevisionHelper;
import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.reporting.AdvancedReporting;
import at.alladin.rmbt.shared.reporting.PeakReport;
import at.alladin.rmbt.shared.reporting.TcpUdpPortsReport;
import at.alladin.rmbt.shared.reporting.VoipPortsReport;
import com.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContextListener implements ServletContextListener {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);

    private static final String SQL_UPDATE_TEST = "UPDATE test SET country_geoip=? WHERE uid=? and (now() - time  < interval '2' minute)";

    private static final String SQL_SELECT_TEST = "SELECT uid,client_public_ip FROM test WHERE client_public_ip IS NOT NULL AND country_geoip IS NULL";

    private static final String SQL_UPDATE_TEST_WITH_INTERVAL = "UPDATE test SET client_public_ip = NULL, public_ip_rdns = NULL, source_ip = NULL, client_ip_local = NULL "
            + "WHERE time < NOW() - CAST('4 months' AS INTERVAL) "
            + "AND (client_public_ip IS NOT NULL OR public_ip_rdns IS NOT NULL OR source_ip IS NOT NULL OR client_ip_local IS NOT NULL)";

    private static final String SQL_UPDATE_TEST_UID = "UPDATE test_ndt n SET main = NULL, stat = NULL, diag = NULL FROM test t "
            + "WHERE t.uid = n.test_id AND t.time < NOW() - CAST('4 months' AS INTERVAL) "
            + "AND (n.main IS NOT NULL OR n.stat IS NOT NULL OR n.diag IS NOT NULL)";

    private static final String SQL_UPDATE_STATUS = "UPDATE status SET ip = NULL "
            + "WHERE time < NOW() - CAST('4 months' AS INTERVAL) "
            + "AND (ip IS NOT NULL)";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        scheduler.shutdownNow();
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @SuppressWarnings("unused")
    private void getGeoIPs() {
        scheduler.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("getting geoips");
                    final Connection conn = DbConnection.getConnection();

                    final boolean oldAutoCommitState = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    // allow update only 2min after test was started
                    final PreparedStatement psUpd = conn.prepareStatement(SQL_UPDATE_TEST);
                    final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_TEST);
                    logger.debug(ps.toString());
                    ps.execute();
                    final ResultSet rs = ps.getResultSet();
                    int count = 0;
                    while (rs.next()) {
                        Thread.sleep(5);
                        count++;
                        if ((count % 1000) == 0)
                            logger.debug(count + " geoips updated");
                        final long uid = rs.getLong("uid");
                        final String ip = rs.getString("client_public_ip");
                        final InetAddress ia = InetAddresses.forString(ip);
                        final String country = GeoIPHelper.getInstance().lookupCountry(ia);
                        if (country != null) {
                            psUpd.setString(1, country);
                            psUpd.setLong(2, uid);
                            logger.debug(psUpd.toString());
                            psUpd.executeUpdate();
                        }
                    }
                    // close result set
                    SQLHelper.closeResultSet(rs);

                    // close prepared statement
                    SQLHelper.closePreparedStatement(psUpd);

                    // close prepared statement
                    SQLHelper.closePreparedStatement(ps);

                    conn.commit();
                    conn.setAutoCommit(oldAutoCommitState);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        logger.debug("RMBTControlServer - " + RevisionHelper.getVerboseRevision());

        try {
            Classification.initInstance(DbConnection.getConnection());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        //init AdvancedReporting:
        AdvancedReporting.init(TcpUdpPortsReport.class, VoipPortsReport.class, PeakReport.class);

        //check log directories
        LogResource.checkLogDirectories();

        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("Cleaning IPs");
                    final Connection conn = DbConnection.getConnection();
                    //purge test table
                    PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_TEST_WITH_INTERVAL);
                    logger.debug(ps.toString());
                    ps.executeUpdate();

                    // close prepared statement
                    SQLHelper.closePreparedStatement(ps);

                    //purge ndt table
                    ps = conn.prepareStatement(SQL_UPDATE_TEST_UID);
                    logger.debug(ps.toString());
                    ps.executeUpdate();

                    // close prepared statement
                    SQLHelper.closePreparedStatement(ps);

                    //purge status table
                    ps = conn.prepareStatement(SQL_UPDATE_STATUS);
                    logger.debug(ps.toString());
                    ps.executeUpdate();

                    // close prepared statement
                    SQLHelper.closePreparedStatement(ps);

                    // close connection
                    SQLHelper.closeConnection(conn);

                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }, 0, 24, TimeUnit.HOURS);
    }
}
