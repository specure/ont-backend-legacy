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

import at.alladin.rmbt.shared.ErrorList;
import com.google.common.net.InetAddresses;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LogResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(LogResource.class);

    public enum Platform {
        CLI,
        IOS,
        APPLET,
        ANDROID,
        RMBT_CONTROL_SERVER,
        RMBT_MAP_SERVER,
        RMBT_STATISTIC_SERVER

    }

    private final static File LOG_PATH = new File(System.getProperty("catalina.base") + "/logs/nettest");

    public static void checkLogDirectories() {
        if (!LOG_PATH.exists()) {
            LOG_PATH.mkdirs();
        }

        for (Platform platform : Platform.values()) {
            File logPath = new File(LOG_PATH, platform.toString().toLowerCase(Locale.US));
            if (!logPath.exists()) {
                logPath.mkdirs();
            }
        }

        logger.debug("Nettest log dir: " + LOG_PATH);
    }

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        final String clientIpRaw = getIP();
        final InetAddress clientAddress = InetAddresses.forString(clientIpRaw);

        logger.debug("Log request from " + clientIpRaw);

        if (entity != null && !entity.isEmpty()) {
            try {
                request = new JSONObject(entity);

                final Platform platform = Platform.valueOf(request.getString("plattform").toUpperCase(Locale.US));
                final String logFileName = request.getString("logfile");

                final File logPath = new File(LOG_PATH, platform.name().toLowerCase(Locale.US));
                final File logFile = new File(logPath, new File(logFileName).getName());
                logger.debug("Writing data into file:" + logFile.getAbsolutePath());


                final boolean appendClientInfo = !logFile.exists();

                try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, logFile.exists())))) {
                    final String content = request.getString("content");
                    request.remove("content");

                    if (appendClientInfo) {
                        out.println("client IP: " + clientAddress.toString());
                        out.println("\n#############################################\n");
                        out.println(request.toString(3));
                        out.println("\n#############################################\n");
                    }
                    out.println(content);
                }


                final JSONObject fileTimes = request.optJSONObject("file_times");
                if (fileTimes != null) {
                    try {
                        final Long created = fileTimes.getLong("created");
                        final Long lastAccess = fileTimes.getLong("last_access");
                        final Long lastModified = fileTimes.getLong("last_modified");
                        BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(logFile.getAbsolutePath()), BasicFileAttributeView.class);
                        FileTime lastModifiedTime = FileTime.fromMillis(TimeUnit.MILLISECONDS.convert(lastModified, TimeUnit.SECONDS));
                        FileTime lastAccessTime = FileTime.fromMillis(TimeUnit.MILLISECONDS.convert(lastAccess, TimeUnit.SECONDS));
                        FileTime createdTime = FileTime.fromMillis(TimeUnit.MILLISECONDS.convert(created, TimeUnit.SECONDS));
                        attributes.setTimes(lastModifiedTime, lastAccessTime, createdTime);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }

//       				final Long lastModified = fileTimes.getLong("last_modified");
//       				logFile.setLastModified(TimeUnit.MILLISECONDS.convert(lastModified, TimeUnit.SECONDS));
                }


                answer.put("status", "OK");
            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSON Data " + e.toString());
            } catch (final Exception e) {
                errorList.addError("ERROR_LOG_WRITE");
                logger.error("Error writing Log " + e.toString());
            }
        } else {
            errorList.addErrorString("Expected request is missing.");
        }

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }

        answerString = answer.toString();

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answerString;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}
