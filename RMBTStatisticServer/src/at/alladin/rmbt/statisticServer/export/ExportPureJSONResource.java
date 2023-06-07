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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.restlet.data.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class ExportPureJSONResource extends ExportResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ExportPureJSONResource.class);

    @Override
    protected MediaType getMediaType() {
        return MediaType.APPLICATION_JSON;
    }


    @Override
    protected String getFileName() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%.json";
    }

    @Override
    protected String getFileNameCurrent() {
        return FILENAME_PREFIX + ".json";
    }

    @Override
    protected String getFileNameZip() {
        return FILENAME_PREFIX + "-%YEAR%-%MONTH%-json.zip";
    }

    @Override
    protected String getFileNameZipCurrent() {
        return FILENAME_PREFIX + "-json.zip";
    }

    @Override
    protected Boolean getZipped() {
        return false;
    }

    @Override
    void writeToFile(OutputStream os, ExportData exportData) throws IOException {

        logger.debug("Started writing to " + getFileName());

        JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.setIndent("  ");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // start array
        writer.beginArray();

        // iterate through all data
        for (int i = 0; i < exportData.data.size(); i++) {

            // create json object
            JsonObject jsonObject = new JsonObject();
            // iterate through all columns
            for (int j = 0; j < exportData.columns.length; j++) {
                jsonObject.add(exportData.columns[j], gson.toJsonTree(exportData.data.get(i)[j]));
            }

            // insert object
            gson.toJson(jsonObject, writer);
        }

        // end array
        writer.endArray();

        // flush
        writer.flush();

        logger.debug("Finished writing to " + getFileName());
    }

}
