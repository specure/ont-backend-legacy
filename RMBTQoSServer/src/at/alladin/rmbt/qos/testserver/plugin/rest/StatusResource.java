/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.qos.testserver.plugin.rest;

import at.alladin.rmbt.qos.testserver.TestServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class StatusResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(StatusResource.class);

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Get
    public String request() throws JSONException {
        JSONObject json = new JSONObject();

        // start time
        json.put("starttime", dateFormatter.format(TestServer.serverPreferences.getStartTimestamp()));
        // version of QoS server
        json.put("version", TestServer.TEST_SERVER_VERSION_MAJOR + "." + TestServer.TEST_SERVER_VERSION_MINOR + "." + TestServer.TEST_SERVER_VERSION_PATCH);
        // timestamp of build
        json.put("build_timestamp", getBuildTimestamp());
        // debian package info
        json.put("deb_package_version", getDebPackageVersion());

        logger.info(json.toString());

        return json.toString();
    }

    private String getDebPackageVersion() {
        String debPackageVersion = "unknown";
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder("apt", "show", "ont-rmbt-server");
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process p = builder.start();
            try (BufferedReader buf =
                         new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = buf.readLine()) != null) {
                    if (line.contains("Version")) {
                        debPackageVersion = line.replace("Version: ", "");
                        return debPackageVersion;
                    }
                }
            }
            p.waitFor();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (process != null)
                process.destroy();
        }

        return debPackageVersion;
    }

    private String getBuildTimestamp() {
        String buildTimestamp = "unknown";
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                Attributes attr = manifest.getMainAttributes();
                buildTimestamp = attr.getValue("Build-Timestamp");
                if (buildTimestamp != null && !buildTimestamp.isEmpty()) {
                    return buildTimestamp;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return buildTimestamp;
    }
}
