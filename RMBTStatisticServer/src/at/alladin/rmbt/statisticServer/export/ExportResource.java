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
package at.alladin.rmbt.statisticServer.export;

import at.alladin.rmbt.shared.SQLHelper;
import at.alladin.rmbt.shared.SettingsHelper;
import at.alladin.rmbt.statisticServer.ServerResource;
import com.specure.rmbt.shared.res.customer.CustomerResource;
import org.apache.commons.io.IOUtils;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class ExportResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ExportResource.class);

//    private final String customerName = CustomerResource.getInstance().getCustomer();

    protected static final String FILENAME_PREFIX = "opendata";

//    private static final String OPEN_DATA_SOURCE_SPECURE = "Open Nettest by Specure-GmbH, Austria";//"=HYPERLINK(\"https://nettest.org/\",\"Open Nettest by Specure-GmbH, Austria\")";
//    private static final String OPEN_DATA_SOURCE_RTR = "RTR-Netztest by RTR-GmbH, Austria";//"=HYPERLINK(\"https://www.netztest.at/en/\",\"RTR-Netztest by RTR-GmbH, Austria\")";
//    private static final String OPEN_DATA_SOURCE_AKOS = "AKOS Test Net by AKOS, Slovenia";//"=HYPERLINK(\"https://www.akostest.net/sl/\",\"AKOS Test Net by AKOS, Slovenia\")";
//    private static final String OPEN_DATA_SOURCE_CTU = "Netmetr by CZ.NIC, Czech Republic";//"=HYPERLINK(\"https://www.netmetr.cz/cs/\",\"Netmetr by CZ.NIC, Czech Republic\")";
//    private static final String OPEN_DATA_SOURCE_RU = "Merac Internetu by Specure-GmbH, Slovakia";//"=HYPERLINK(\"https://www.meracinternetu.sk/sk/index\",\"Merac Internetu by Specure-GmbH, Slovakia\")";
//    private static final String OPEN_DATA_SOURCE_RATEL = "Ratel NetTest by Ratel, Serbia";

//    private static final String CELL_LINK_LICENSE_CC_BY_4_0 = "CC BY 4.0";//"=HYPERLINK(\"https://creativecommons.org/licenses/by/4.0/\",\"CC BY 4.0\")";
//    private static final String CELL_LINK_LICENSE_CC_BY_3_0_AT = "CC BY 3.0 AT";//"=HYPERLINK(\"https://creativecommons.org/licenses/by-sa/3.0/at/\",\"CC BY 3.0 AT\")";
//    private static final String CELL_LINK_LICENSE_CC_BY_NC_SA_2_5_SI = "CC BY-NC-SA 2.5 SI";// "=HYPERLINK(\"https://creativecommons.org/licenses/by-nc-sa/2.5/si/\",\"CC BY-NC-SA 2.5 SI\")";
//    private static final String CELL_LINK_LICENSE_CC_BY_3_0_CZ = "CC BY 3.0 CZ";// "=HYPERLINK(\"https://creativecommons.org/licenses/by/3.0/cz/\",\"CC BY 3.0 CZ\")";

    private static final String FILENAME_LICENSE_CC_BY_4_0 = "CC BY 4.0";
//    private static final String FILENAME_LICENSE_CC_BY_3_0_AT = "CC BY 3.0 AT";
//    private static final String FILENAME_LICENSE_CC_BY_NC_SA_2_5_SI = "CC BY-NC-SA 2.5 SI";
//    private static final String FILENAME_LICENSE_CC_BY_3_0_CZ = "CC BY 3.0 CZ";

    //private static final String FILENAME = "nettest-opendata-%YEAR%-%MONTH%.";
    //private static final String FILENAME_ZIP = "nettest-%YEAR%-%MONTH%.zip";
    //private static final String FILENAME_CURRENT = "nettest-opendata.";
    //private static final String FILENAME_ZIP_CURRENT = "nettest-opendata.zip";
    abstract protected String getFileName();
    abstract protected String getFileNameZip();
    abstract protected String getFileNameCurrent();
    abstract protected String getFileNameZipCurrent();
    abstract protected MediaType getMediaType();
    abstract protected Boolean getZipped();

    //private static final boolean zip = true;

    /**
     * if true, the current export will write all data to the csv, if false only the last 31 days will be exported
     */
    private static final boolean allowFullExport = false;

    private static final long cacheThresholdMs = 60 * 60 * 1000; //1 hour

    public final static String NEW_EXPORT_SQL_WITH_DATE = "SELECT * from get_opendata_by_date(?, ?, ?)"; // year, month, accuracy
    public final static String NEW_EXPORT_SQL_WITH_DATE_RANGE = "SELECT * from get_opendata_by_daterange(?, ?, ?)"; // timestampFrom, timestampTo, accuracy
    public final static String NEW_EXPORT_SQL_FULL = "SELECT * from get_opendata(?, ?)"; // fullexport, accuracy
    public final static String NEW_EXPORT_SQL_WITH_OPEN_TEST_UUID = "SELECT * from get_opendata_by_open_test_uuid(?, ?)"; // uuid, accuracy

    public static final AdditionalReportFieldsData reportDataFields = new AdditionalReportFieldsData(new String[]{
            //"open_data_source", "license"
    });
//
//    public final static String EXPORT_SQL = "SELECT" +
//            "  ('O' || t.open_test_uuid)                                   open_test_uuid," +
//            "  to_char(t.time AT TIME ZONE 'UTC', 'YYYY-MM-DD HH24:MI:SS') as time," +
//            "  nt.type                                                     network_group_type," +
//            "  t.speed_upload                                              speed_upload," +
//            "  t.speed_download                                            speed_download," +
//            "  t.ping_median                                                    ping_median," +
//            "  t.network_operator_name                                       network_operator_name," +
//            "  t.network_sim_operator                                        network_sim_operator_name," +
//            "  t.public_ip_as_name                                           public_ip_as_name," +
//            "  t.network_is_roaming                                         network_is_roamning," +
//            "  roaming_type                                                roaming_type," +
//            "  client_public_ip_anonymized                                 client_public_ip_anonymized," +
//            "  (CASE WHEN publish_public_data" +
//            "    THEN t.device" +
//            "   ELSE NULL END)                                             device," +
//            "  (CASE WHEN publish_public_data" +
//            "    THEN COALESCE(adm.fullname, t.model)" +
//            "   ELSE NULL END)                                             model," +
//            //"  product                                                     product," +
//            "  (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder')" +
//            "    THEN t.geo_lat" +
//            "   WHEN (t.geo_accuracy < ?)" +
//            "     THEN ROUND(t.geo_lat * 1111) / 1111" +
//            "   ELSE NULL END)                                             geo_lat," +
//            "  (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder')" +
//            "    THEN t.geo_long" +
//            "   WHEN (t.geo_accuracy < ?)" +
//            "     THEN ROUND(t.geo_long * 741) / 741" +
//            "   ELSE NULL END)                                             geo_long," +
//            "  (CASE WHEN ((t.geo_provider = 'manual') OR (t.geo_provider = 'geocoder'))" +
//            "    THEN 'rastered'" +
//            "   ELSE t.geo_provider END)                                   geo_provider," +
//            "  (CASE WHEN (t.geo_accuracy < ?) AND (t.geo_provider != 'manual') AND (t.geo_provider != 'geocoder')" +
//            "    THEN t.geo_accuracy" +
//            "   WHEN (t.geo_accuracy < 100) AND ((t.geo_provider = 'manual') OR (t.geo_provider = 'geocoder'))" +
//            "     THEN 100" +
//            "   WHEN (t.geo_accuracy < ?)" +
//            "     THEN t.geo_accuracy" +
//            "   ELSE NULL END)                                             geo_accuracy," +
//            "  t.network_sim_country                                       network_sim_country," +
//            "  t.country_asn                                               country_asn," +
//            "  t.country_location                                          country_location," +
//            "  t.country_geoip                                             country_geoip," +
//            "  t.zero_measurement                                          zero_measurement, ";

    @Get
    public Representation request(final String entity) {

        logger.debug("rquest: " + entity);
        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");

        final String filename_zip;
        final String filename;

        //allow filtering by month/year
        int year = -1;
        int month = -1;
        int yearFrom = -1;
        int monthFrom = -1;
        int dayFrom = -1;
        int yearTo = -1;
        int monthTo = -1;
        int dayTo = -1;
        if (getRequest().getAttributes().containsKey("yearFrom") && getRequest().getAttributes().containsKey("monthFrom") && getRequest().getAttributes().containsKey("dayFrom")
                && getRequest().getAttributes().containsKey("yearTo") && getRequest().getAttributes().containsKey("monthTo") && getRequest().getAttributes().containsKey("dayTo")) {
            // ONT-1915 - Data export with data range - legacy portal

            try {
                yearFrom = Integer.parseInt(getRequest().getAttributes().get("yearFrom").toString());
                monthFrom = Integer.parseInt(getRequest().getAttributes().get("monthFrom").toString());
                dayFrom = Integer.parseInt(getRequest().getAttributes().get("dayFrom").toString());
                yearTo = Integer.parseInt(getRequest().getAttributes().get("yearTo").toString());
                monthTo = Integer.parseInt(getRequest().getAttributes().get("monthTo").toString());
                dayTo = Integer.parseInt(getRequest().getAttributes().get("dayTo").toString());
            } catch (NumberFormatException ex) {
                //logger.error(ex.getMessage());
                //Nothing -> fallback to normal request at this point
            }
            if (yearFrom > -1 && monthFrom > -1 && dayFrom > -1 && yearTo > -1 && monthTo > -1 && dayTo > -1) {
//                filename_zip = getFileNameZip().replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
//                filename = getFileName().replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
                String fromString = yearFrom + "-" + String.format("%02d", monthFrom) + "-" + String.format("%02d", dayFrom);
                String toString = yearTo + "-" + String.format("%02d", monthTo) + "-" + String.format("%02d", dayTo);
                filename_zip = getFileNameZip().replace("%YEAR%", fromString).replace("%MONTH%", toString);
                filename = getFileName().replace("%YEAR%", fromString).replace("%MONTH%", toString);
            } else {
                filename_zip = getFileNameZipCurrent();
                filename = getFileNameCurrent();
            }
        } else if (getRequest().getAttributes().containsKey("year")) {
            try {
                year = Integer.parseInt(getRequest().getAttributes().get("year").toString());
                month = Integer.parseInt(getRequest().getAttributes().get("month").toString());
            } catch (NumberFormatException ex) {
                //logger.error(ex.getMessage());
                //Nothing -> fallback to normal request at this point
            }
            if (year < 2099 && month > 0 && month <= 12 && year > 2000) {
                filename_zip = getFileNameZip().replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));
                filename = getFileName().replace("%YEAR%", Integer.toString(year)).replace("%MONTH%", String.format("%02d", month));

            } else {
                filename_zip = getFileNameZipCurrent();
                filename = getFileNameCurrent();
            }
        } else {
            filename_zip = getFileNameZipCurrent();
            filename = getFileNameCurrent();
        }

        final File cachedFile = new File(property + File.separator + ((getZipped()) ? filename_zip : filename));
        final File generatingFile = new File(property + File.separator + ((getZipped()) ? filename_zip : filename) + "_tmp");
        if (cachedFile.exists()) {

            //check if file has been recently created OR a file is currently being created
            if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
                    (generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                //if so, return the cached file instead of a cost-intensive new one
                final OutputRepresentation result = new OutputRepresentation(getZipped() ? MediaType.APPLICATION_ZIP
                        : getMediaType()) {

                    @Override
                    public void write(OutputStream out) throws IOException {
                        InputStream is = new FileInputStream(cachedFile);
                        IOUtils.copy(is, out);
                        out.close();
                    }

                };
                if (getZipped()) {
                    final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(filename_zip);
                    result.setDisposition(disposition);
                }
                return result;

            }
        }

        try {
            boolean zippedFile = getZipped();
            double accuracy = Double.parseDouble(SettingsHelper.getSetting(conn, "rmbt_geo_accuracy_detail_limit", null));
            PreparedStatement ps;
            if (yearFrom > 0 && monthFrom > 0 && dayFrom > 0 && monthFrom <= 12 && yearFrom > 2000 && dayFrom <= 31
                && yearTo > 0 && monthTo > 0 && dayTo > 0 && monthTo <= 12 && yearTo > 2000 && dayTo <= 31) {
                // ONT-1915 - Data export with data range - legacy portal

                Calendar cal = new GregorianCalendar();
                cal.set(Calendar.YEAR, yearFrom);
                cal.set(Calendar.MONTH, monthFrom - 1);
                cal.set(Calendar.DATE, dayFrom);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Timestamp timestampFrom = new Timestamp(cal.getTimeInMillis());

                cal.set(Calendar.YEAR, yearTo);
                cal.set(Calendar.MONTH, monthTo - 1);
                cal.set(Calendar.DATE, dayTo);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                Timestamp timestampTo = new Timestamp(cal.getTimeInMillis());

                // check timestamps
                if(timestampFrom.after(timestampTo)) {
                    logger.error("Entered date 'from' {0} is after entered date 'to' {1}!", timestampFrom, timestampTo);
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Entered date 'from' is after entered date 'to'!");
                    return null;
                }

                ps = conn.prepareStatement(NEW_EXPORT_SQL_WITH_DATE_RANGE);
                ps.setTimestamp(1, timestampFrom);
                ps.setTimestamp(2, timestampTo);
                ps.setDouble(3, accuracy);

                // not zipped file
                zippedFile = false;
            } else if ((year > 0 && month > 0 && month <= 12 && year > 2000)) {
                ps = conn.prepareStatement(NEW_EXPORT_SQL_WITH_DATE);
                ps.setInt(1, year);
                ps.setInt(2, month);
                ps.setDouble(3, accuracy);
            } else {
                ps = conn.prepareStatement(NEW_EXPORT_SQL_FULL);
                ps.setBoolean(1, allowFullExport);
                ps.setDouble(2, accuracy);
            }

            final ExportData exportData = query(ps, reportDataFields);

            // ONT-1915 - Data export with data range - legacy portal
            //final OutputRepresentation result = getExportFile(exportData, filename_zip, filename, getZipped(), true);
            final OutputRepresentation result = getExportFile(exportData, filename_zip, filename, zippedFile, true);

//            if (zippedFile) {
//                final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
//                disposition.setFilename(filename_zip);
//                result.setDisposition(disposition);
//            } else {
//                final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
//                disposition.setFilename(filename);
//                result.setDisposition(disposition);
//            }
            final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(zippedFile ? filename_zip : filename);
            result.setDisposition(disposition);

            logger.debug("rsponse: " + result.toString());

            return result;
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static class AdditionalReportFieldsData {
        final String[] columns;

        public AdditionalReportFieldsData(final String[] columns) {
            this.columns = columns;
        }

        public String[] getColumns() {
            return columns;
        }
    }

    public static class ExportData {
        final String[] columns;
        final List<String[]> data;

        public ExportData(final String[] columns, final List<String[]> data) {
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

    public OutputRepresentation getExportFile(final ExportData exportData, final String filename, final boolean isCached) {
        return getExportFile(exportData, filename, filename, true, isCached);
    }

    public OutputRepresentation getExportFile(final ExportData exportData, final String filenameZip, final String filename, final boolean isZipped, final boolean isCached) {
        final OutputRepresentation result = new OutputRepresentation(isZipped ? MediaType.APPLICATION_ZIP : getMediaType()) {
            @Override
            public void write(OutputStream out) throws IOException {
                //cache in file => create temporary temporary file (to 
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                File cachedFile = null;
                OutputStream outf = out;

                if (isZipped && isCached) {
                    cachedFile = new File(property + File.separator + ((isZipped) ? filenameZip : filename) + "_tmp");
                    outf = new FileOutputStream(cachedFile);
                }

                final ZipOutputStream zos = new ZipOutputStream(outf);

                if (isZipped) {

                    // CC BY 4.0
                    final ZipEntry zeLicenseSPECURE = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_4_0 + ".txt");
                    zos.putNextEntry(zeLicenseSPECURE);
                    final InputStream licenseISSPECURE = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE.txt");
                    IOUtils.copy(licenseISSPECURE, zos);
                    licenseISSPECURE.close();

                    final ZipEntry zeCsv = new ZipEntry(filename);
                    zos.putNextEntry(zeCsv);
                    outf = zos;

//                    if (customerName != null && (customerName.toLowerCase().equals("specure") || customerName.toLowerCase().equals("ont"))) {
//                        // ont or specure
//
//                        // CC BY 3.0 AT
//                        final ZipEntry zeLicenseRTR = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_3_0_AT + ".txt");
//                        zos.putNextEntry(zeLicenseRTR);
//                        final InputStream licenseISRTR = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_rtr.txt");
//                        IOUtils.copy(licenseISRTR, zos);
//                        licenseISRTR.close();
//
//                        // CC BY 3.0 CZ
//                        final ZipEntry zeLicenseCTU = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_3_0_CZ + ".txt");
//                        zos.putNextEntry(zeLicenseCTU);
//                        final InputStream licenseISCTU = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_ctu.txt");
//                        IOUtils.copy(licenseISCTU, zos);
//                        licenseISCTU.close();
//
//                        // CC BY-NC-SA 2.5 SI
//                        final ZipEntry zeLicenseAKOS = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_NC_SA_2_5_SI + ".txt");
//                        zos.putNextEntry(zeLicenseAKOS);
//                        final InputStream licenseISAKOS = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_akos.txt");
//                        IOUtils.copy(licenseISAKOS, zos);
//                        licenseISAKOS.close();
//
//                        // CC BY 4.0
//                        final ZipEntry zeLicenseSPECURE = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_4_0 + ".txt");
//                        zos.putNextEntry(zeLicenseSPECURE);
//                        final InputStream licenseISSPECURE = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE.txt");
//                        IOUtils.copy(licenseISSPECURE, zos);
//                        licenseISSPECURE.close();
//
//                        final ZipEntry zeCsv = new ZipEntry(filename);
//                        zos.putNextEntry(zeCsv);
//                        outf = zos;
//
//                    } else if (customerName != null && customerName.toLowerCase().equals("ru")) {
//                        // ru-sk
//
//                        // CC BY 4.0
//                        final ZipEntry zeLicenseRU = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_4_0 + ".txt");
//                        zos.putNextEntry(zeLicenseRU);
//                        final InputStream licenseISRU = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_ru.txt");
//                        IOUtils.copy(licenseISRU, zos);
//                        licenseISRU.close();
//
//                        final ZipEntry zeCsv = new ZipEntry(filename);
//                        zos.putNextEntry(zeCsv);
//                        outf = zos;
//
//                    } else if (customerName != null && customerName.toLowerCase().equals("ratel")) {
//                        // ratel-rs
//
//                        // CC BY 4.0
//                        final ZipEntry zeLicenseRatel = new ZipEntry("LICENSE " + FILENAME_LICENSE_CC_BY_4_0 + ".txt");
//                        zos.putNextEntry(zeLicenseRatel);
//                        final InputStream licenseISRatel = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_ratel.txt");
//                        IOUtils.copy(licenseISRatel, zos);
//                        licenseISRatel.close();
//
//                        final ZipEntry zeCsv = new ZipEntry(filename);
//                        zos.putNextEntry(zeCsv);
//                        outf = zos;
//                    } else {
//                        // rtr-at, akos-sl, ctu-cz
//
//                        // standard license
//                        final ZipEntry zeLicense = new ZipEntry("LICENSE.txt");
//                        zos.putNextEntry(zeLicense);
//                        final InputStream licenseIS = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_" + customerName + ".txt");
//                        IOUtils.copy(licenseIS, zos);
//                        licenseIS.close();
//
//                        final ZipEntry zeCsv = new ZipEntry(filename);
//                        zos.putNextEntry(zeCsv);
//                        outf = zos;
//                    }

                }


                writeToFile(outf, exportData);

                if (isZipped) {
                    outf.close();
                }

                if (isZipped && isCached) {
                    //if we reach this code, the data is now cached in a temporary tmp-file
                    //so, rename the file for "production use2
                    //concurrency issues should be solved by the operating system
                    File newCacheFile = new File(property + File.separator + ((isZipped) ? filenameZip : filename));
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

    abstract void writeToFile(OutputStream os, ExportData exportData) throws IOException;


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
        //insert filter for accuracy
        double accuracy = Double.parseDouble(SettingsHelper.getSetting(conn, "rmbt_geo_accuracy_detail_limit", null));
        return ps;
    }

    public ExportData query(final PreparedStatement ps, final AdditionalReportFieldsData reportFieldsData) {
        final String[] columns;
        final List<String[]> data = new ArrayList<>();
        ResultSet rs = null;
        try {
            logger.debug(ps.toString());
            if (!ps.execute())
                return null;
            rs = ps.getResultSet();

            final ResultSetMetaData meta = rs.getMetaData();
            // by KGB - fixed error - last column was not added to the file
            //final int colCnt = meta.getColumnCount() - 1;
            final int colCnt = meta.getColumnCount();
            final int colCntInclusiveReportFields = colCnt + (reportFieldsData != null ? reportFieldsData.getColumns().length : 0);
            columns = new String[colCntInclusiveReportFields];
            for (int i = 0; i < colCnt; i++) {
                columns[i] = meta.getColumnName(i + 1);
            }

            for (int i = colCnt; i < colCntInclusiveReportFields; i++) {
                columns[i] = reportFieldsData.columns[i - colCnt];
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

        return new ExportData(columns, data);
    }
}
