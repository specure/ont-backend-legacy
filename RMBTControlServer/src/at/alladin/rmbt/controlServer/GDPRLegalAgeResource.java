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

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.ErrorList;
import at.alladin.rmbt.shared.SQLHelper;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class GDPRLegalAgeResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(GDPRLegalAgeResource.class);

    private static final String SELECT_LEGAL_AGE_BY_COUNTRY_CODE = "SELECT age FROM gdpr_legal_age WHERE country_code = ?";
    private static final String SELECT_DEFAULT_LEGAL_AGE_ = "SELECT age FROM gdpr_legal_age WHERE country_code = 'default'";

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " + entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        // DB staff
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (entity != null && entity.isEmpty() == false) {

            // get country code
            request = new JSONObject(entity);
            String countryCode = request.optString("country_code", "default");

            try {

                // create connection and statement
                conn = DbConnection.getConnection();
                ps = conn.prepareStatement(SELECT_LEGAL_AGE_BY_COUNTRY_CODE);
                ps.setString(1, countryCode);

                // execute query
                logger.debug(ps.toString());
                rs = ps.executeQuery();

                // check result set
                if (rs.next() == true) {
                    // found age, put it into answer

                    answer.put("age", rs.getInt("age"));
                    answer.put("country_code", countryCode);

                } else {
                    // not found, select default

                    ps = conn.prepareStatement(SELECT_DEFAULT_LEGAL_AGE_);

                    // execute query
                    logger.debug(ps.toString());
                    rs = ps.executeQuery();

                    // check result set
                    if (rs.next() == true) {
                        answer.put("age", rs.getInt("age"));
                        answer.put("country_code", "default");
                    }

                }

                SQLHelper.closeResultSet(rs);
                SQLHelper.closePreparedStatement(ps);
                SQLHelper.closeConnection(conn);

            } catch (Exception ex) {
                logger.error(ex.getMessage());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                errorList.addError("ERROR_DB_CONNECTION");
                answer.putOpt("error", errorList.getList());
            }

        } else {
            // no country code entered, try to read default age

            try {

                // create connection and statement
                conn = DbConnection.getConnection();
                ps = conn.prepareStatement(SELECT_DEFAULT_LEGAL_AGE_);

                // execute query
                logger.debug(ps.toString());
                rs = ps.executeQuery();

                // check result set
                if (rs.next() == true) {
                    answer.put("age", rs.getInt("age"));
                    answer.put("country_code", "default");
                }

                SQLHelper.closeResultSet(rs);
                SQLHelper.closePreparedStatement(ps);
                SQLHelper.closeConnection(conn);

            } catch (Exception ex) {
                logger.error(ex.getMessage());
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                errorList.addError("ERROR_DB_CONNECTION");
                answer.putOpt("error", errorList.getList());
            }

        }

        answer.putOpt("error", errorList.getList());
        answerString = answer.toString();

        // log response
        logger.debug("rsponse: " + answerString);

        return answerString;
    }

    @Post("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}