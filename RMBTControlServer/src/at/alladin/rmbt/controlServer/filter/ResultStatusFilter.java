/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
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
 *******************************************************************************/
package at.alladin.rmbt.controlServer.filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultStatusFilter extends Filter {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ResultStatusFilter.class);

    @Override
    protected void afterHandle(Request request, Response response) {
        logger.debug("STATUS FILTER");
        try {
            final JSONObject json = new JSONObject(response.getEntityAsText());
            final JSONArray errorArray = json.optJSONArray("error");
            if (errorArray != null && errorArray.length() > 0) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL);
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        super.afterHandle(request, response);
    }
}
