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

import at.alladin.rmbt.shared.RevisionHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(VersionResource.class);

    @Get("json")
    public String request(final String entity) {
        // log request
        logger.debug("rquest: " +entity);

        try {
            final JSONObject answer = new JSONObject();
            answer.put("version", RevisionHelper.getVerboseRevision());
//            answer.put("gitId", RevisionHelper.getServerVersion());

            // log response
            logger.debug("rsponse: " + answer.toString());

            return answer.toString();
        } catch (JSONException e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
