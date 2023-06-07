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
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.TestNdt;
import at.alladin.rmbt.shared.db.fields.LongField;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class NdtResultResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(NdtResultResource.class);

    @Post("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        addAllowOrigin();
        final JSONObject answer = new JSONObject();

        try {
            final JSONObject request = new JSONObject(entity);

            final String testUuidString;

            // TODO remove test_token
            if (request.has("test_token")) {
                final String[] token = request.getString("test_token").split("_");
                testUuidString = token[0];
            } else if (request.has("test_uuid")) {
                testUuidString = request.getString("test_uuid");
            } else
                throw new IllegalArgumentException();

            final Test test = new Test(conn);
            final UUID testUuid = UUID.fromString(testUuidString);
            final long testId = test.getTestByUuid(testUuid);
            if (testId < 0)
                throw new IllegalArgumentException();

            final TestNdt ndt = new TestNdt(conn);
            ((LongField) ndt.getField("test_id")).setValue(testId);
            ndt.setFields(request);
            ndt.storeTest();

        } catch (final IllegalArgumentException e) {
            logger.error(e.toString());
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            answer.put("error","Invalid UUID");
            logger.error(answer.toString());
            return answer.toString();
        } catch (final JSONException e) {
            logger.error(e.toString());
        }

        // log response
        logger.debug("rsponse: " + answer.toString());

        return answer.toString();
    }
}
