/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.alladin.rmbt.shared;

import at.alladin.rmbt.shared.db.fields.Field;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class JsonUtil {

    protected ResourceBundle labels;
    protected boolean optionWithKeys;

    public JsonUtil(final ResourceBundle labels, final boolean optionWithKeys) {
        this.labels = labels;
        this.optionWithKeys = optionWithKeys;
    }

    public JSONObject addObject(final JSONArray array, final String key) throws JSONException {
        final JSONObject newObject = new JSONObject();
        newObject.put("title", getKeyTranslation(key));

        if (optionWithKeys) {
            newObject.put("key", key);
        }

        array.put(newObject);
        return newObject;
    }

    public void addJSONObject(final JSONArray array, final String title, final JSONObject value)
            throws JSONException {
        if (value != null && value.length() > 0) {
            final JSONObject json = addObject(array, title).put("value", value);

            final JSONObject titlesJson = new JSONObject();
            final Iterator<String> keys = value.keys();

            while (keys.hasNext()) {
                final String key = keys.next();
                titlesJson.put(key, getKeyTranslation(title + "." + key));
            }

            json.put("title", titlesJson);
        }
    }

    public void addString(final JSONArray array, final String title, final String value)
            throws JSONException {
        if (value != null && !value.isEmpty()) {
            addObject(array, title).put("value", value);
        }
    }

    public void addString(final JSONArray array, final String title, final Field field)
            throws JSONException {
        if (!field.isNull())
            addString(array, title, field.toString());
    }

    public void addInt(final JSONArray array, final String title, final Field field)
            throws JSONException {
        if (!field.isNull())
            addObject(array, title).put("value", field.intValue());
    }

    public void addLong(final JSONArray array, final String title, final Field field)
            throws JSONException {
        if (!field.isNull()) {
            addObject(array, title).put("value", field.longValue());
        }
    }

    public String getTranslation(final String prefix, final String key) {
        try {
            return labels.getString(prefix + "_" + key);
        } catch (final MissingResourceException e) {
            return key;
        }
    }

    public String getKeyTranslation(final String key) {
        return getTranslation("key", key);
    }

    public ResourceBundle getLabels() {
        return labels;
    }

    public void setLabels(ResourceBundle labels) {
        this.labels = labels;
    }

    public boolean isOptionWithKeys() {
        return optionWithKeys;
    }

    public void setOptionWithKeys(boolean optionWithKeys) {
        this.optionWithKeys = optionWithKeys;
    }
}
