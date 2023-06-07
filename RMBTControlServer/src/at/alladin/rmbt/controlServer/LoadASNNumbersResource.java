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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

public class LoadASNNumbersResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(LoadASNNumbersResource.class);

    private static final String SQL_SELECT_PROVIDER_ID =
            "SELECT provider_id from as2provider where asn = ?";
    private static final String SQL_SELECT_MAX_ID_FROM_PROVIDER =
            "select max(uid) as next from provider";
    private static final String SQL_CHECK_EXIST_ASN2COUNTRY =
            "SELECT 1 from asn2country where asn = ?";
    private static final String SQL_INSERT_ASN2COUNTRY =
            "Insert into asn2country (asn, country) values(?,?)";
    private static final String SQL_INSERT_PROVIDER =
            "Insert into provider (uid, name, map_filter) values(?,?,False)";
    private static final String SQL_INSERT_AS2PROVIDER =
            "Insert into as2provider (asn, provider_id) values(?,?)";

    @Post("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();
        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;
        int count = 0;

        logger.info("Starting Load ASN Providers");

        if (entity != null && !entity.isEmpty()) {
            try {
                request = new JSONObject(entity);
                JSONArray list = request.getJSONArray("asn_list");

                final Connection conn = DbConnection.getConnection();
                final PreparedStatement ps = conn.prepareStatement(SQL_SELECT_PROVIDER_ID);


                final PreparedStatement psnext = conn.prepareStatement(SQL_SELECT_MAX_ID_FROM_PROVIDER);
                logger.debug(psnext.toString());
                psnext.execute();
                final ResultSet rsnext = psnext.getResultSet();
                int next_provider_id = 0;
                if (rsnext.next()) {
                    next_provider_id = rsnext.getInt("next");
                    SQLHelper.closeResultSet(rsnext);
                    SQLHelper.closePreparedStatement(psnext);
                } else {
                    SQLHelper.closeResultSet(rsnext);
                    SQLHelper.closePreparedStatement(psnext);
                    throw new Exception("Could not get data from provider.");
                }

                ResultSet rs = null;
                for (int i = 0; i < list.length() && i < 10; i++) {
                    JSONObject provider = list.getJSONObject(i);
                    final String ASNNumber = provider.getString("ASN");
                    final String Name = provider.getString("Name");
                    final String Country = provider.getString("Country");

                    logger.debug(String.format("%d> AS %d %s, %s", ++count, Integer.parseInt(ASNNumber.substring(2)), Name, Country));

                    //final PreparedStatement psUpd = conn.prepareStatement("UPDATE test SET country_geoip=? WHERE uid=? and (now() - time  < interval '2' minute)");
                    ps.setInt(1, Integer.parseInt(ASNNumber.substring(2)));
                    logger.debug(ps.toString());
                    ps.execute();
                    rs = ps.getResultSet();

                    if (rs.next()) {
                        final int provider_id = rs.getInt("provider_id");
                        logger.info("- exist");
                        final PreparedStatement psac = conn.prepareStatement(SQL_CHECK_EXIST_ASN2COUNTRY);
                        psac.setInt(1, Integer.parseInt(ASNNumber.substring(2)));
                        logger.debug(psac.toString());
                        psac.execute();
                        final ResultSet rsac = psac.getResultSet();
                        if (rsac.next()) {
                            logger.info(" as2country OK");
                        } else {
                            logger.info(" as2country inserting");
                            final PreparedStatement psacin = conn.prepareStatement(SQL_INSERT_ASN2COUNTRY);
                            psacin.setInt(1, Integer.parseInt(ASNNumber.substring(2)));
                            psacin.setString(2, Country);
                            logger.debug(psacin.toString());
                            if (psacin.executeUpdate() == 0)
                                throw new Exception("Could not insert into as2provider");

                            // close prepared statement
                            SQLHelper.closePreparedStatement(psacin);
                        }

                        // close result set
                        SQLHelper.closeResultSet(rsac);

                        // close prepared statement
                        SQLHelper.closePreparedStatement(psac);

                    } else {
                        logger.info(" new");
                        next_provider_id++;
                        //insert into provider
                        final PreparedStatement psipr = conn.prepareStatement(SQL_INSERT_PROVIDER);
                        psipr.setInt(1, next_provider_id);
                        psipr.setString(2, Name);
                        logger.debug(psipr.toString());
                        psipr.executeUpdate();

                        // close prepared statement
                        SQLHelper.closePreparedStatement(psipr);

                        //insert into as2provider
                        final PreparedStatement psias2pr = conn.prepareStatement(SQL_INSERT_AS2PROVIDER);
                        psias2pr.setInt(1, Integer.parseInt(ASNNumber.substring(2)));
                        psias2pr.setInt(2, next_provider_id);
                        logger.debug(psias2pr.toString());
                        psias2pr.executeUpdate();

                        // close prepared statement
                        SQLHelper.closePreparedStatement(psias2pr);

                        //inserv into asn2country
                        final PreparedStatement psias2c = conn.prepareStatement(SQL_INSERT_ASN2COUNTRY);
                        psias2c.setInt(1, Integer.parseInt(ASNNumber.substring(2)));
                        psias2c.setString(2, Country);
                        logger.debug(psias2c.toString());
                        psias2c.executeUpdate();

                        // close prepared statement
                        SQLHelper.closePreparedStatement(psias2c);

                    }
                    logger.info(" - done");
                }
                // close result set
                SQLHelper.closeResultSet(rs);

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);

                answer.put("status", "OK");
            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error(" Error parsing JSON Data " + e.toString());
            } catch (final Exception e) {
                logger.error(" Error Loading ASN Numbers " + e.toString());
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

        logger.debug("rsponse: " + answerString);

        return answerString;
    }

    @Get("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}