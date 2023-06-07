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
package at.alladin.rmbt.qos.testserver.util;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.service.EventJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class RuntimeGuardService extends EventJob<String> {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeGuardService.class);

    public final static String TAG = RuntimeGuardService.class.getCanonicalName();
    public final static String GUARD_FILE = "guard.properties";
    public final static String PROPERTY_KEY_STATUS = "status";
    public final static String PROPERTY_VALUE_STATUS_UP = "up";
    public final static String PROPERTY_VALUE_STATUS_DOWN = "down";
    public final static String PROPERTY_KEY_STARTUP = "last_startup";
    public final static String PROPERTY_KEY_SHUTDOWN = "last_shutdown";
    protected Properties prop = new Properties();

    public RuntimeGuardService() {
        super(TestServerServiceEnum.RUNTIME_GUARD_SERVICE);
        getLaunchEventSet().add(EventType.ON_TEST_SERVER_START);
        getLaunchEventSet().add(EventType.ON_TEST_SERVER_STOP);

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(GUARD_FILE));
        } catch (FileNotFoundException e) {
            logger.info("I/O Error: Guard file '" + GUARD_FILE + "' not found! Creating new guard file...");
            try {
                savePropertyFile(true);
            } catch (IOException e1) {
                logger.info("I/O Error: Guard file '" + GUARD_FILE + "' could not be created!");
            }
        } catch (IOException e) {
            logger.info("I/O Error: Cannot open guard file!");
        }
    }

    @Override
    public String execute() throws Exception {
        if (prop != null) {
            switch (getLastEventType()) {
                case ON_TEST_SERVER_START:
                    String status = prop.getProperty(PROPERTY_KEY_STATUS, PROPERTY_VALUE_STATUS_DOWN);
                    if (!status.equals(PROPERTY_VALUE_STATUS_DOWN)) {
                        logger.info("Test server shutdown not executed correctly!");
                    } else {
                        prop.setProperty(PROPERTY_KEY_STATUS, PROPERTY_VALUE_STATUS_UP);
                        prop.setProperty(PROPERTY_KEY_STARTUP, (new Date()).toString());
                    }
                    savePropertyFile();
                    return null;
                case ON_TEST_SERVER_STOP:
                    String startUp = prop.getProperty(PROPERTY_KEY_STARTUP);
                    logger.info("Test server was up since: " + startUp);
                    prop.setProperty(PROPERTY_KEY_STATUS, PROPERTY_VALUE_STATUS_DOWN);
                    prop.setProperty(PROPERTY_KEY_SHUTDOWN, (new Date()).toString());
                    savePropertyFile();
                    return null;
                default:
                    break;
            }
        }

        return "Execution error!";
    }

    private void savePropertyFile() throws IOException {
        savePropertyFile(false);
    }

    private void savePropertyFile(boolean isNew) throws IOException {
        if (isNew) {
            prop.setProperty(PROPERTY_KEY_STATUS, PROPERTY_VALUE_STATUS_DOWN);
            prop.setProperty(PROPERTY_KEY_STARTUP, (new Date()).toString());
        }
        prop.store(new FileOutputStream(GUARD_FILE), "Automatically generated guard file.\nPlease do not change manually!");
    }

    @Override
    public RuntimeGuardService getNewInstance() {
        return new RuntimeGuardService();
    }

    @Override
    public String getId() {
        return TAG;
    }
}
