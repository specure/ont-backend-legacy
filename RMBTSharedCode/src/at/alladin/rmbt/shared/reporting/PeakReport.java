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
package at.alladin.rmbt.shared.reporting;

import at.alladin.rmbt.shared.SpeedGraph;
import at.alladin.rmbt.shared.SpeedGraph.SpeedGraphItem;
import at.alladin.rmbt.shared.db.QoSTestResult;
import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.fields.IntField;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class PeakReport implements AdvancedReport {

    @Override
    public void addSpeedtestReport(final Test test, final JSONObject testResultJson, final JSONObject reportJson) throws JSONException {
        try {
            final IntField numThreadsField = (IntField) test.getField("num_threads");
            final IntField numThreadsUlField = (IntField) test.getField("num_threads_ul");

            final int numThreads = numThreadsField != null ? numThreadsField.intValue() : 0;
            final int numThreadsUl = numThreadsUlField != null ? numThreadsUlField.intValue() : 0;

            SpeedGraph speedGraph = new SpeedGraph(test.getUid(), Math.max(numThreads, numThreadsUl), test.getConnection());

            double peak = 0d;

            for (final SpeedGraphItem i : speedGraph.getDownload()) {
                peak = Math.max(peak, ((double) i.getBytesTotal() / (double) i.getTimeElapsed()));
            }

            reportJson.put("peak_down_kbit", (int) (8 * peak));

            peak = 0d;
            for (final SpeedGraphItem i : speedGraph.getUpload()) {
                peak = Math.max(peak, ((double) i.getBytesTotal() / (double) i.getTimeElapsed()));
            }

            reportJson.put("peak_up_kbit", (int) (8 * peak));

        } catch (SQLException e) {
            //some error while generating report = ignore
        }
    }

    @Override
    public void addQoSReport(List<QoSTestResult> qosResultList, JSONObject reportJson) throws JSONException {
        //nothing to do with qos-test data
    }

}
