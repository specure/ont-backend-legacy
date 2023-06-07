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
import at.alladin.rmbt.shared.hstoreparser.HstoreParseException;
import at.alladin.rmbt.shared.qos.QoSUtil;
import at.alladin.rmbt.shared.qos.QoSUtil.TestUuid;
import at.alladin.rmbt.shared.qos.QoSUtil.TestUuid.UuidType;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class QoSResultResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(QoSResultResource.class);

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();
        String answerString;

        logger.debug("New QoS test result detail request from " + getIP());

        if (entity != null && !entity.isEmpty())
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                String lang = request.optString("language");

                // Load Language Files for Client

                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                } else
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");


                if (conn != null) {
                    final String testUuid = request.optString("test_uuid");
                    long ts = System.nanoTime();
                    QoSUtil.evaluate(settings, conn, new TestUuid(testUuid, UuidType.TEST_UUID), answer, lang, errorList);
                    long endTs = System.nanoTime() - ts;
                    answer.put("evaluation", "Time needed to evaluate test result: " + ((float) endTs / 1000000f) + " ms");
                } else {
                    errorList.addError("ERROR_DB_CONNECTION");
                }

            } catch (final JSONException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error(e.getMessage());
            } catch (SQLException e) {
                errorList.addError("ERROR_DB_CONNECTION");
                logger.error(e.getMessage());
            } catch (HstoreParseException e) {
                errorList.addErrorString(e.getMessage());
                logger.error(e.getMessage());

            } catch (IllegalArgumentException e) {
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error(e.getMessage());
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage());
            } catch (UnsupportedOperationException e) {
                //e.printStackTrace();
                errorList.addError("ERROR_REQUEST_QOS_RESULT_DETAIL_NO_UUID");
                logger.error(e.getMessage());
            }

        else
            errorList.addErrorString("Expected request is missing.");

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
