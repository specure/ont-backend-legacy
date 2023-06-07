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
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.shared.db.QoSTestDesc;
import at.alladin.rmbt.shared.db.QoSTestObjective;
import at.alladin.rmbt.shared.db.dao.QoSTestDescDao;
import at.alladin.rmbt.shared.db.dao.QoSTestObjectiveDao;
import com.specure.rmbt.shared.res.customer.CustomerResource;
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
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class QualityOfServiceExportResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(QualityOfServiceExportResource.class);

    private static final String FILENAME_HTML = "netztest-nndata.html";
    private static final String FILENAME_ZIP = "netztest-nndata.zip";

    private static final boolean zip = false;

    private static final long cacheThresholdMs = 10; // 60*60*1000; //1 hour

    private final String customer = CustomerResource.getInstance().getCustomer();

    @Get
    public Representation request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        //Before doing anything => check if a cached file already exists and is new enough
        String property = System.getProperty("java.io.tmpdir");
        final File cachedFile = new File(property + File.separator + ((zip) ? FILENAME_ZIP : FILENAME_HTML));
        final File generatingFile = new File(property + File.separator + ((zip) ? FILENAME_ZIP : FILENAME_HTML) + "_tmp");
        if (cachedFile.exists()) {

            //check if file has been recently created OR a file is currently being created
            if (((cachedFile.lastModified() + cacheThresholdMs) > (new Date()).getTime()) ||
                    (generatingFile.exists() && (generatingFile.lastModified() + cacheThresholdMs) > (new Date()).getTime())) {

                //if so, return the cached file instead of a cost-intensive new one
                final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP
                        : MediaType.TEXT_HTML) {

                    @Override
                    public void write(OutputStream out) throws IOException {
                        InputStream is = new FileInputStream(cachedFile);
                        IOUtils.copy(is, out);
                        out.close();
                    }

                };
                if (zip) {
                    final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                    disposition.setFilename(FILENAME_ZIP);
                    result.setDisposition(disposition);
                }
                return result;

            }
        }


        //final List<String> data = new ArrayList<String>();
        final StringBuilder sb = new StringBuilder();
        //create html headers
        sb.append("<html class=\"no-js\" lang=\"\" data-ng-app=\"nettestApp\" data-ng-controller=\"CustomerController as customerctrl\"> <head> <base href=\"/\" /> <meta charset=\"utf-8\"> </head> ");

        QoSTestObjectiveDao nnObjectiveDao = new QoSTestObjectiveDao(conn);
        QoSTestDescDao nnDescDao = new QoSTestDescDao(conn, null);

        try {
            Map<String, List<QoSTestObjective>> map = nnObjectiveDao.getAllToMap();
            Iterator<String> keys = map.keySet().iterator();
            sb.append("<h1>Contents:</h1>");
            sb.append("<ol>");
            sb.append("<li><a href=\"#table1\">qos_test_objective</a></li>");
            sb.append("<li><a href=\"#table2\">qos_test_desc</a></li>");
            sb.append("</ol><br>");

            sb.append("<h1 id=\"table1\">Test objectives table (qos_test_objective)</h1>");
            while (keys.hasNext()) {
                String testType = keys.next();
                List<QoSTestObjective> list = map.get(testType);
                sb.append("<h2>Test group: " + testType.toUpperCase() + "</h2><ul>");
                //data.add("<h2>Test group: " + testType.toUpperCase() + "</h2>");
                for (QoSTestObjective item : list) {
                    //data.add(item.toHtml());
                    sb.append("<li>");
                    sb.append(item.toHtml());
                    sb.append("</li>");
                }
                sb.append("</ul>");
            }

            Map<String, List<QoSTestDesc>> descMap = nnDescDao.getAllToMapIgnoreLang();
            keys = descMap.keySet().iterator();
            sb.append("<h1 id=\"table2\">Language table (qos_test_desc)</h1><ul>");
            while (keys.hasNext()) {
                String descKey = keys.next();
                List<QoSTestDesc> list = descMap.get(descKey);
                sb.append("<li><h4 id=\"" + descKey.replaceAll("[\\-\\+\\.\\^:,]", "_") + "\">KEY (column: desc_key): <i>" + descKey + "</i></h4><ul>");
                //data.add("<h3>KEY: <i>" + descKey + "</i></h3><ul>");
                for (QoSTestDesc item : list) {
                    sb.append("<li><i>" + item.getLang() + "</i>: " + item.getValue() + "</li>");
                    //data.add("<li><i>" + item.getLang() + "</i>: " + item.getValue() + "</li>");
                }
                sb.append("</ul></li>");
                //data.add("</ul>");
            }
            sb.append("</ul>");

        } catch (final SQLException e) {
            logger.error(e.getMessage());
            return null;
        }

        final OutputRepresentation result = new OutputRepresentation(zip ? MediaType.APPLICATION_ZIP : MediaType.TEXT_HTML) {
            @Override
            public void write(OutputStream out) throws IOException {
                //cache in file => create temporary temporary file (to 
                // handle errors while fulfilling a request)
                String property = System.getProperty("java.io.tmpdir");
                final File cachedFile = new File(property + File.separator + ((zip) ? FILENAME_ZIP : FILENAME_HTML) + "_tmp");
                OutputStream outf = new FileOutputStream(cachedFile);

                if (zip) {
                    final ZipOutputStream zos = new ZipOutputStream(outf);
                    final ZipEntry zeLicense = new ZipEntry("LICENSE.txt");
                    zos.putNextEntry(zeLicense);
                    final InputStream licenseIS = CustomerResource.getInstance().getResourceAsStream("DATA_LICENSE_" + customer + ".txt");
                    IOUtils.copy(licenseIS, zos);
                    licenseIS.close();

                    final ZipEntry zeCsv = new ZipEntry(FILENAME_HTML);
                    zos.putNextEntry(zeCsv);
                    outf = zos;
                }

                try (OutputStreamWriter osw = new OutputStreamWriter(outf)) {
                    osw.write(sb.toString());
                    osw.flush();
                }

                if (zip)
                    outf.close();

                //if we reach this code, the data is now cached in a temporary tmp-file
                //so, rename the file for "production use2
                //concurrency issues should be solved by the operating system
                File newCacheFile = new File(property + File.separator + ((zip) ? FILENAME_ZIP : FILENAME_HTML));
                Files.move(cachedFile.toPath(), newCacheFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

                FileInputStream fis = new FileInputStream(newCacheFile);
                IOUtils.copy(fis, out);
                fis.close();
                out.close();
            }
        };
        if (zip) {
            final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
            disposition.setFilename(FILENAME_ZIP);
            result.setDisposition(disposition);
        }

        // log response
        logger.debug("rsponse: " + result.toString());

        return result;
    }
}
