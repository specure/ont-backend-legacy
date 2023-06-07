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
import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.db.Client;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ClientRegistrationResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ClientRegistrationResource.class);

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " + entity);

        long startTime = System.currentTimeMillis();
        addAllowOrigin();

        JSONObject request = null;

        final ErrorList errorList = new ErrorList();
        final JSONObject answer = new JSONObject();

        final String clientIpRaw = getIP();
        logger.info("New client registration request from " + clientIpRaw);


        if (entity != null && !entity.isEmpty()) {
            // try parse the string to a JSON object
            try {
                request = new JSONObject(entity);

                String lang = request.optString("language");

                // Load Language Files for Client
                final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));

                if (langs.contains(lang)) {
                    errorList.setLanguage(lang);
                    labels = ResourceManager.getSysMsgBundle(new Locale(lang));
                } else {
                    lang = settings.getString("RMBT_DEFAULT_LANGUAGE");
                }


                if (conn != null) {

                    Client client = new Client(conn);
                    UUID uuid = null;

                    // get type: MOBILE or DESKTOP
                    int typeId = 0;
                    if (request.optString("type").length() > 0) {
                        typeId = client.getTypeId(request.getString("type"));
                        if (client.hasError()) {
                            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                            errorList.addError(client.getError());
                            logger.error(client.getError());
                        }

                        // get terms and conditions acceptance
                        boolean tcAccepted = request.optInt("terms_and_conditions_accepted_version", 0) > 0; // accept any version for now
                        if (!tcAccepted) {// allow old non-version parameter
                            tcAccepted = request.optBoolean("terms_and_conditions_accepted", false);
                        }

                        client.setTimeZone(Helperfunctions.getTimeWithTimeZone(Helperfunctions.getTimezoneId()));
                        client.setTime(Timestamp.valueOf(new Timestamp(System.currentTimeMillis()).toString()));
                        client.setClient_type_id(typeId);
                        client.setTcAccepted(tcAccepted);

                        uuid = client.storeClient();

                        if (client.hasError()) {
                            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                            errorList.addError(client.getError());
                        } else {
                            answer.put("uuid", uuid.toString());
                        }

                    } else {
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                        errorList.addError("ERROR_MISSING_EXPECTED_REQUEST");
                        logger.error("Expected request is missing.");
                    }

                } else {
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                    errorList.addError("ERROR_DB_CONNECTION");
                    logger.error("Could not connect to database.");
                }

            } catch (final JSONException e) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                errorList.addError("ERROR_REQUEST_JSON");
                logger.error("Error parsing JSDON Data " + e.toString());
            }

        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            errorList.addErrorString("ERROR_MISSING_EXPECTED_REQUEST");
            logger.error("Expected request is missing.");
        }

        try {
            answer.putOpt("error", errorList.getList());
        } catch (final JSONException e) {
            logger.error("Error saving ErrorList: " + e.toString());
        }


        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }

    @Post("json")
    public String retrieve(final String entity) {
        return request(entity);
    }

}
