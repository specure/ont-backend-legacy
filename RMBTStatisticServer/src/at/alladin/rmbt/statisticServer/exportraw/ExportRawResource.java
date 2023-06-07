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
package at.alladin.rmbt.statisticServer.exportraw;

import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.Settings;
import at.alladin.rmbt.statisticServer.ServerResource;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportRawResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ExportRawResource.class);

    private static final String customerName = CustomerResource.getInstance().getCustomer();

    private static final String FILENAME_CSV = "nettest-rawdata-%YEAR%-%MONTH%.csv";
    private static final String FILENAME_ZIP = "nettest-%YEAR%-%MONTH%.zip";
    private static final String FILENAME_CSV_CURRENT = "nettest-rawdata.csv";
    private static final String FILENAME_ZIP_CURRENT = "nettest-rawdata.zip";

    private static final CSVFormat csvFormat = CSVFormat.RFC4180;
    private static final boolean zip = true;

    /**
     * if true, the current export will write all data to the csv, if false only the last 31 days will be exported
     */
    private static final boolean allowFullExport = false;

    private static final long cacheThresholdMs = 60 * 60 * 1000; //1 hour

//    SELECT cli.uuid as           klient_uuid,
//    t.*,
//    xp.avg_ping,
//    xs.*,
//    xgl.*,
//    ts.cpu_usage,
//    ts.mem_usage,
//    ci.result_band_name          band_name,
//    ci.result_bandwidth          bandwith,
//    ci.result_band               band_channel,
//    ci.result_frequency_download band_frequency_download,
//    ci.result_frequency_upload   band_frequency_upload
//    FROM test t
//    LEFT JOIN client cli ON t.client_id = cli.uid
//    LEFT JOIN (select avg(value) avg_ping, p.test_id from ping p group by p.test_id) xp on xp.test_id = t.uid
//    LEFT JOIN (select avg(s.signal_strength) avg_signal_strength,
//    avg(wifi_link_speed)   avg_wifi_link_speed,
//    avg(wifi_rssi)         avg_wifi_rssi,
//    avg(lte_rsrp)          avg_lte_rsrp,
//    avg(lte_rsrq)          avg_lte_rsrq,
//    avg(lte_rssnr)         avg_wifi_rssnr,
//    avg(lte_cqi)           avg_wifi_cqi,
//    s.test_id
//    from signal s
//    group by s.test_id) xs ON xs.test_id = t.uid
//    LEFT JOIN (select avg(gl.accuracy) agv_accuracy,
//    avg(gl.altitude) avg_altitude,
//    avg(gl.bearing)  avg_bearing,
//    avg(gl.speed)    avg_speed,
//    avg(geo_lat)     geo_lat,
//    avg(geo_long)    geo_long,
//    gl.test_id
//    from geo_location gl
//    where gl.provider = 'gps'
//    group by gl.test_id) xgl on xgl.test_id = t.uid
//    LEFT JOIN test_stat ts on ts.test_uid = t.uid
//    LEFT JOIN cell_info ci on ci.test_uid = t.uid
//    WHERE t.deleted = false
//    AND (EXTRACT(month FROM t.time AT TIME ZONE 'UTC') = 10)
//    AND (EXTRACT(year FROM t.time AT TIME ZONE 'UTC') = 2020)
//    AND status = 'FINISHED'
//    ORDER BY t.uid DESC

    public final static String EXPORT_SQL = "SELECT "
            + "cli.uuid as klient_uuid, t.*,"
            + " xp.avg_ping,"
            + " xs.*,"
            + " xgl.*,"
            + " ci.result_band_name band_name, ci.result_bandwidth defined_bandwidth, ci.result_band band_number, ci.result_frequency_download band_frequency_download, ci.result_frequency_upload band_frequency_upload,"
            + " ts.cpu_usage, ts.mem_usage";

    public final static String EXPORT_SQL_SUFFIX =
            " FROM test t "
                    + " LEFT JOIN client cli ON t.client_id=cli.uid"
                    //add pings to result
                    + " LEFT JOIN ( select avg (value) avg_ping, p.test_id from ping p group by p.test_id) xp on xp.test_id  = t.uid"
                    //add signal to result
                    + " LEFT JOIN ( select"
                    + " avg (s.signal_strength) avg_signal_strength,"
                    + " avg(wifi_link_speed) avg_wifi_link_speed,"
                    + " avg(wifi_rssi) avg_wifi_rssi,"
                    + " avg(lte_rsrp) avg_lte_rsrp,"
                    + " avg(lte_rsrq) avg_lte_rsrq,"
                    + " avg(lte_rssnr) avg_wifi_rssnr,"
                    + " avg(lte_cqi) avg_wifi_cqi,"
                    + " s.test_id from signal s group by s.test_id) xs "
                    + " ON xs.test_id  = t.uid"
                    // add goe location to result
                    + " LEFT JOIN (select"
                    + " avg(gl.accuracy) agv_accuracy,"
                    + " avg(gl.altitude) avg_altitude,"
                    + " avg(gl.bearing ) avg_bearing,"
                    + " avg(gl.speed) avg_speed,"
                    + " avg(geo_lat) geo_lat,"
                    + " avg(geo_long) geo_long,"
                    + " gl.test_id"
                    + " from geo_location gl"
                    + " where gl.provider = 'gps'"
                    + " group by gl.test_id) xgl on xgl.test_id  = t.uid"
                    //add test stat data to result ( CPU , RAM usage in JSONs )
                    + " LEFT JOIN cell_info ci on ci.test_uid = t.uid"
                    + " LEFT JOIN test_stat ts on ts.test_uid = t.uid";

          /*          " LEFT JOIN network_type nt ON nt.uid=t.network_type" +
          " LEFT JOIN device_map adm ON adm.codename=t.model" +
          " LEFT JOIN test_server ts ON ts.uid=t.server_id" +
          " LEFT JOIN test_ndt ndt ON t.uid=ndt.test_id";
*/

    @Get
    public Representation request(final String entity) {
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");

        final String filename_zip;
        final String filename_csv;

        //allow filtering by month/year
        //https://s01.meracinternetu.sk/RMBTStatisticServer/admin/exportraw/nettest-rawdata-2016-07.zip
        int year = -1;
        int month = -1;
        if (getRequest().getAttributes().containsKey("year")) {
            try {
                year = Integer.parseInt(getRequest().getAttributes().get("year").toString());
                month = Integer.parseInt(getRequest().getAttributes().get("month").toString());
            } catch (NumberFormatException ex) {
                //logger.error(ex.getMessage());
                //Nothing -> fallback to normal request at this point
            }
            if (year < 2099 && month > 0 && month <= 12 && year > 2000) {
                filename_zip = FILENAME_ZIP.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
                filename_csv = FILENAME_CSV.replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));

            } else {
                filename_zip = FILENAME_ZIP_CURRENT;
                filename_csv = FILENAME_CSV_CURRENT;
            }
        } else {
            filename_zip = FILENAME_ZIP_CURRENT;
            filename_csv = FILENAME_CSV_CURRENT;
        }


        final File cachedFile = new File(property + File.separator + ((zip) ? filename_zip : filename_csv));
        final File generatingFile = new File(property + File.separator + ((zip) ? filename_zip : filename_csv) + "_tmp");
        if (cachedFile.exists()) {

            //check if file has been recently created OR a file is currently being created
            if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
                    (generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                //if so, return the cached file instead of a cost-intensive new one
                final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP
                        : MediaType.TEXT_CSV) {

                    @Override
                    public void write(OutputStream out) throws IOException {
                        InputStream is = new FileInputStream(cachedFile);
                        IOUtils.copy(is, out);
                        out.close();
                    }

                };
                if (zip) {
                    final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(filename_zip);
                    result.setDisposition(disposition);
                }
                return result;

            }
        }

        final String timeClause = (year > 0 && month > 0 && month <= 12 && year > 2000) ?
                " AND (EXTRACT (month FROM t.time AT TIME ZONE 'UTC') = " + month + ") AND (EXTRACT (year FROM t.time AT TIME ZONE 'UTC') = " + year + ") "
                : (allowFullExport ? "" : " AND t.time > current_date - interval '31 days' ");

        final String sql = getExportSql(this) +
                " WHERE " +
                " t.deleted = false" +
                timeClause +
                " AND status = 'FINISHED'" +
                " ORDER BY t.uid";

        //System.out.println(sql);
        logger.info("sql: {}", sql);

        try {
            final PreparedStatement ps = createPreparedStatement(sql, conn);

            final ExportDataRaw exportDataRaw = query(ps);

            final OutputRepresentation result = getExportFile(exportDataRaw, filename_zip, filename_csv, zip, true);

            if (zip) {
                final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                disposition.setFilename(filename_zip);
                result.setDisposition(disposition);
            }

            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static class ExportDataRaw {
        final String[] columns;
        final List<String[]> data;

        public ExportDataRaw(final String[] columns, final List<String[]> data) {
            this.columns = columns;
            this.data = data;
        }

        public String[] getColumns() {
            return columns;
        }

        public List<String[]> getData() {
            return data;
        }
    }

    public static OutputRepresentation getExportFile(final ExportDataRaw exportDataRaw, final String filename, final boolean isCached) {
        return getExportFile(exportDataRaw, filename, filename, true, isCached);
    }

    public static OutputRepresentation getExportFile(final ExportDataRaw exportDataRaw, final String filenameZip, final String filenameCsv, final boolean isZipped, final boolean isCached) {
        final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP : MediaType.TEXT_CSV) {
            @Override
            public void write(OutputStream out) throws IOException {
                //cache in file => create temporary temporary file (to 
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                File cachedFile = null;
                OutputStream outf = out;

                if (isCached) {
                    cachedFile = new File(property + File.separator + ((zip) ? filenameZip : filenameCsv) + "_tmp");
                    outf = new FileOutputStream(cachedFile);
                }

                if (isZipped) {
                    final ZipOutputStream zos = new ZipOutputStream(outf);
                    final ZipEntry zeLicense = new ZipEntry("LICENSE.txt");
                    zos.putNextEntry(zeLicense);

                    final InputStream licenseIS = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE.txt");
                    IOUtils.copy(licenseIS, zos);
                    licenseIS.close();

                    final ZipEntry zeCsv = new ZipEntry(filenameCsv);
                    zos.putNextEntry(zeCsv);

                    outf = zos;
                }

                final OutputStreamWriter osw = new OutputStreamWriter(outf);
                final CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);

                for (final String c : exportDataRaw.getColumns())
                    csvPrinter.print(c);
                csvPrinter.println();

                for (final String[] line : exportDataRaw.getData()) {
                    for (final String f : line)
                        csvPrinter.print(f);
                    csvPrinter.println();
                }
                csvPrinter.flush();

                if (isZipped)
                    outf.close();

                if (isCached) {
                    //if we reach this code, the data is now cached in a temporary tmp-file
                    //so, rename the file for "production use2
                    //concurrency issues should be solved by the operating system
                    File newCacheFile = new File(property + File.separator + ((zip) ? filenameZip : filenameCsv));
                    Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    FileInputStream fis = new FileInputStream(newCacheFile);
                    IOUtils.copy(fis, out);
                    fis.close();
                    out.close();
                }
            }
        };

        return result;
    }

    /**
     * creates a prepared statement where the first 6 parameters are already set (accuracy)
     *
     * @param sql
     * @param conn
     * @return
     * @throws SQLException
     */
    public static PreparedStatement createPreparedStatement(final String sql, final Connection conn) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(sql);

        return ps;
    }

    public static ExportDataRaw query(final PreparedStatement ps) {
        final String[] columns;
        final List<String[]> data = new ArrayList<>();
        ResultSet rs = null;
        try {
            logger.debug(ps.toString());
            if (!ps.execute())
                return null;
            rs = ps.getResultSet();

            final ResultSetMetaData meta = rs.getMetaData();
            final int colCnt = meta.getColumnCount() - 1;
            final int colCntInclusiveReportFields = colCnt;
            columns = new String[colCntInclusiveReportFields];
            for (int i = 0; i < colCnt; i++) {
                columns[i] = meta.getColumnName(i + 1);
            }


            while (rs.next()) {
                final String[] line = new String[colCntInclusiveReportFields];

                for (int i = 0; i < colCnt; i++) {
                    final Object obj = rs.getObject(i + 1);
                    line[i] = obj == null ? null : obj.toString();
                }

                data.add(line);
            }
        } catch (final SQLException e) {
            logger.error(e.getMessage());
            return null;
        } finally {

            // close result set
            SQLHelper.closeResultSet(rs);

            // close prepared statement
            SQLHelper.closePreparedStatement(ps);

        }

        return new ExportDataRaw(columns, data);
    }

    public static String getExportSql(final Settings settings) {
// AGR removed advertisedspeedoption, export all columns from DB
//    	final String hasAdvSpeedOption = settings.getSetting("has_advertised_speed_option");

        return EXPORT_SQL +
/*    			((hasAdvSpeedOption != null && Boolean.parseBoolean(hasAdvSpeedOption)) ?
                        "t.adv_spd_option_name advertised_internet_connection, " +
    					"t.adv_spd_up_kbit advertised_up_kbit, " + 
    					"t.adv_spd_down_kbit advertised_down_kbit, " +
    					"(t.adv_spd_up_kbit - t.speed_upload) deviation_advertised_up_kbit, " +
    					"(t.adv_spd_down_kbit - t.speed_download) deviation_advertised_down_kbit, "
    					: "") + */
                EXPORT_SQL_SUFFIX;
    }
}
