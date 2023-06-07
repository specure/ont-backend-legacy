/*******************************************************************************
 * Copyright 2013-2017 Specure GmbH
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
package at.alladin.rmbt.statisticServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//import java.util.Date;

public class SumProgress extends at.alladin.rmbt.statisticServer.ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(SumProgress.class);

    @Get("json")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        long testCount = 0;
        long incrementOfUser = 0;
        double incrementOfDay = 0.0;
        double incrementOfHour = 0.0;

        try {
            PreparedStatement psSum = conn.prepareStatement(
                    "select a_tests_count() cntAll, a_clients_count() cntUser, count (*) cntDay  from test where time>(current_date - INTERVAL '5 days')");
            ResultSet rsSum = psSum.executeQuery();
            while (rsSum.next()) {
                testCount = rsSum.getLong("cntAll");
                incrementOfUser = rsSum.getLong("cntUser");
                incrementOfDay = (rsSum.getLong("cntDay") + 0.0) / 5.0;
            }
            incrementOfHour = incrementOfDay / 24.0;
            incrementOfDay = (double) Math.round(incrementOfDay * 100.0) / 100.0;
            incrementOfHour = (double) Math.round(incrementOfHour * 100.0) / 100.0;
        } catch (final SQLException e) {
            logger.error("Error on SumProgress: " + e.toString());
        }
        ;
        try {
            final JSONObject answer = new JSONObject();
            answer.put("testsCount", testCount);
            answer.put("uniqueClients", incrementOfUser);
            answer.put("testsOfDay", incrementOfDay);
            answer.put("testsOfHour", incrementOfHour);

            // log response
            logger.debug("rsponse: " + answer.toString());

            return answer.toString();
        } catch (JSONException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
