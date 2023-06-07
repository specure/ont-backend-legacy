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
package at.alladin.rmbt.shared;

import com.google.gson.JsonArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ErrorList {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ErrorList.class);

    private JSONArray errorList = null;

    private ResourceBundle labels = null;

    public ErrorList() {
        errorList = new JSONArray();
        labels = ResourceManager.getSysMsgBundle();
    }

    public void addError(final String errorLabel) {
        try {
            final String errorText = labels.getString(errorLabel);
            addErrorString(errorText);
        } catch (final MissingResourceException e) {
            logger.warn("Error writing to ErrorList: Label" + errorLabel + "not found in"
                    + labels.getLocale().toString());
            logger.error(e.getMessage());

        } catch (final NullPointerException e) {
            logger.warn("Error writing to ErrorList: Label" + errorLabel + "not found in"
                    + labels.getLocale().toString());
            logger.error(e.getMessage());
        }
    }

    public void addErrorString(final String errorText) {
        try {
            errorList.put(errorList.length(), errorText);
            logger.debug(errorText);
        } catch (final JSONException e) {
            logger.warn("Error writing ErrorList: " + e.toString());
            logger.error(e.getMessage());
        }
    }

    public void setLanguage(final String lang) {
        labels = ResourceManager.getSysMsgBundle(new Locale(lang));
    }

    public int getLength() {
        return errorList.length();
    }

    public boolean isEmpty() {
        return getLength() == 0;
    }

    public JSONArray getList() {
        return errorList;
    }

    public JsonArray getListAsJsonArray() {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < errorList.length(); i++) {
            jsonArray.add(errorList.getString(i));
        }
        return jsonArray;
    }

}
