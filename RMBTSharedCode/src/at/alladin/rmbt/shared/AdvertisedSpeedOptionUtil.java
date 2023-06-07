/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
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
package at.alladin.rmbt.shared;

import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.fields.IntField;
import at.alladin.rmbt.shared.db.fields.LongField;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.Format;

public class AdvertisedSpeedOptionUtil {

    public static void expandWithAdvertisedSpeedStatus(JsonUtil util, Settings settings, final Test test, JSONArray resultList, final Format format) throws JSONException {
        final String advSpdOption = settings.getSetting("has_advertised_speed_option");

        if (advSpdOption != null && Boolean.parseBoolean(advSpdOption)) {
            if (!test.getField("adv_spd_option_name").isNull()) {
                util.addString(resultList, "adv_spd_option_name", test.getField("adv_spd_option_name"));
            }

            if (!test.getField("adv_spd_up_kbit").isNull()) {
                util.addString(resultList, "adv_spd_up_kbit", String.format("%s %s",
                        format.format(((LongField) test.getField("adv_spd_up_kbit")).doubleValue() / 1000d),
                        util.getLabels().getString("RESULT_UPLOAD_UNIT")));
            }
            if (!test.getField("adv_spd_down_kbit").isNull()) {
                util.addString(resultList, "adv_spd_down_kbit", String.format("%s %s",
                        format.format(((LongField) test.getField("adv_spd_down_kbit")).doubleValue() / 1000d),
                        util.getLabels().getString("RESULT_DOWNLOAD_UNIT")));
            }

            if (!test.getField("adv_spd_up_kbit").isNull()) {
                final long deviationUl = ((LongField) test.getField("adv_spd_up_kbit")).longValue() - ((IntField) test.getField("speed_upload")).intValue();
                util.addString(resultList, "adv_spd_deviation_up_kbit",
                        String.format("%s %s", format.format((double) deviationUl / 1000d), util.getLabels().getString("RESULT_UPLOAD_UNIT")));
            }

            if (!test.getField("adv_spd_down_kbit").isNull()) {
                final long deviationDl = ((LongField) test.getField("adv_spd_down_kbit")).longValue() - ((IntField) test.getField("speed_download")).intValue();
                util.addString(resultList, "adv_spd_deviation_down_kbit",
                        String.format("%s %s", format.format((double) deviationDl / 1000d), util.getLabels().getString("RESULT_DOWNLOAD_UNIT")));
            }
        }
    }
}
