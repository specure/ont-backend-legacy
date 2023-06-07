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

import at.alladin.rmbt.shared.SettingsHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class TestExportResource extends ExportResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestExportResource.class);

    @Override
    protected MediaType getMediaType() {
        return MediaType.TEXT_CSV;
    }

    @Override
    protected String getFileName() {
        return "fileName";
    }

    @Override
    protected String getFileNameZip() {
        return "fileNameZip";
    }

    @Override
    protected String getFileNameCurrent() {
        return "fileNameCurrent";
    }

    @Override
    protected String getFileNameZipCurrent() {
        return "fileNameZipCurrent";
    }

    @Override
    protected Boolean getZipped() {
        return true;
    }

    @Get
    public Representation request(final String entity) {

        // log request
        logger.debug("rquest: " + entity);

        try {
            final UUID openUUID = UUID.fromString(getRequest().getAttributes().get("open_test_uuid").toString());

            try {
                final PreparedStatement ps = ExportResource.createPreparedStatement(ExportResource.NEW_EXPORT_SQL_WITH_OPEN_TEST_UUID, conn);
                ps.setObject(1, openUUID.toString());
                ps.setDouble(2, Double.parseDouble(SettingsHelper.getSetting(conn, "rmbt_geo_accuracy_detail_limit", null)));

                final ExportData exportData = query(ps, null);

                final OutputRepresentation result = getExportFile(exportData,
                        "O" + openUUID.toString() + ".zip", "O" + openUUID.toString() + ".csv", true, false);

                final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                disposition.setFilename("O" + openUUID.toString() + ".zip");
                result.setDisposition(disposition);

                logger.debug("rsponse: " + result.toString());

                return result;
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    @Override
    void writeToFile(OutputStream os, ExportData exportData) throws IOException {
        logger.debug("Test Export: Started writing to " + getFileName());

        ExportCSVResource.writeDataToFile(os, exportData);

        logger.debug("Test Export: Finished writing to " + getFileName());
    }

}
