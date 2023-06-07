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
package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SQLHelper;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class ServerResource extends org.restlet.resource.ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ServerResource.class);

    protected Connection conn;
    protected ResourceBundle labels;
    protected ResourceBundle settings;
    protected ResourceBundle countries;
    private static final String SQL_SELECT_JPL = "Select count(uid) from test_jpl where test_uid = ?";

    @Override
    public void doInit() throws ResourceException {
        super.doInit();

        String customer = "specure";
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            customer = (String) environmentContext.lookup("customer");
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }

        settings = ResourceManager.getCfgBundle(new Locale(customer));
        countries = ResourceManager.getCountries();
        // Set default Language for System
        Locale.setDefault(new Locale(settings.getString("RMBT_DEFAULT_LANGUAGE")));
        labels = ResourceManager.getSysMsgBundle();

        // Get DB-Connection
        try {
            conn = DbConnection.getConnection();
        } catch (final NamingException e) {
            logger.error(e.getMessage());
        } catch (final SQLException e) {
            logger.warn(labels.getString("ERROR_DB_CONNECTION_FAILED"));
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        super.doRelease();

        // close connection
        SQLHelper.closeConnection(conn);
    }

    @SuppressWarnings("unchecked")
    protected void addAllowOrigin() {
        Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
        if (responseHeaders == null) {
            responseHeaders = new Series<Header>(Header.class);
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.add("Access-Control-Allow-Credentials", "false");
        responseHeaders.add("Access-Control-Max-Age", "60");
    }

    @Options
    public void doOptions(final Representation entity) {
        addAllowOrigin();
    }

    /**
     * Method checks if exists record in test_jpl table
     *
     * @param test_uid
     * @return
     */
    protected boolean existJPLRecord(String test_uid) {

        boolean resultBoolean = false;

        // check test_uuid
        if (test_uid != null && test_uid.isEmpty() == false) {
            logger.debug("ExistJPL: " + test_uid);


            if (test_uid.isEmpty()) {
                return false;
            }

            try {
                PreparedStatement ps = conn.prepareStatement(SQL_SELECT_JPL);
                ps.setObject(1, UUID.fromString(test_uid));

                ResultSet rs = ps.executeQuery();
                rs.next();

                long countRow = rs.getLong("count");
                if (countRow != 0) {
                    resultBoolean = true;
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }

        return resultBoolean;
    }

}
