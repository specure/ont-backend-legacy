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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.restlet.data.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class ExportCSVResource extends ExportResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ExportCSVResource.class);

    @Override
    protected MediaType getMediaType() {
        return MediaType.TEXT_CSV;
    }

    @Override
    protected String getFileName() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%.csv";
    }

    @Override
    protected String getFileNameCurrent() {
        return FILENAME_PREFIX + ".csv";
    }

    @Override
    protected String getFileNameZip() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%-csv.zip";
    }

    @Override
    protected String getFileNameZipCurrent() {
        return FILENAME_PREFIX + "-csv.zip";
    }

    @Override
    protected Boolean getZipped() {
        return true;
    }

    @Override
    void writeToFile(OutputStream os, ExportData exportData) throws IOException {

        logger.debug("Started writing to " + getFileName());

        ExportCSVResource.writeDataToFile(os, exportData);

        logger.debug("Finished writing to " + getFileName());
    }

    /*
        This method is public static, becasue it is used also by TestExportResource class
     */

    public static void writeDataToFile(OutputStream os, ExportData exportData) throws IOException {
        final OutputStreamWriter osw = new OutputStreamWriter(os);
        final CSVPrinter csvPrinter = new CSVPrinter(osw, CSVFormat.RFC4180);

        for (final String c : exportData.getColumns()) {
            csvPrinter.print(c);
        }
        csvPrinter.println();

        for (final String[] line : exportData.getData()) {
            for (final String f : line) {
                csvPrinter.print(f);
            }
            csvPrinter.println();
        }
        csvPrinter.flush();
    }
}
