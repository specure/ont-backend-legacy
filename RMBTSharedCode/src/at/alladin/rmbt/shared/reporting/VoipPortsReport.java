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

import at.alladin.rmbt.shared.db.QoSTestResult;
import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.qos.VoipResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VoipPortsReport implements AdvancedReport {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(VoipPortsReport.class);

    public final static class PacketStats {
        public int total = 0;
        public int arrived = 0;
    }

    @Override
    public void addSpeedtestReport(final Test test, final JSONObject testResultJson, final JSONObject reportJson) throws JSONException {
        //nothing to do with speed-test data
    }

    @Override
    public void addQoSReport(List<QoSTestResult> qosResultList, JSONObject reportJson) throws JSONException {
        final PacketStats outPackets = new PacketStats();
        final PacketStats inPackets = new PacketStats();

        try {
            for (final QoSTestResult result : qosResultList) {
                if (result.getResult() instanceof VoipResult) {
                    VoipResult voipResult = (VoipResult) result.getResult();

                    outPackets.total += ((int) voipResult.getCallDuration() / (int) voipResult.getDelay());
                    outPackets.arrived = (int) voipResult.getNumPacketsOut();
                    inPackets.total += outPackets.total;
                    inPackets.arrived = (int) voipResult.getNumPacketsIn();
                }
            }

            reportJson.put("packet_loss_down", (int) (100f * ((float) (inPackets.total - inPackets.arrived) / (float) inPackets.total)));
            reportJson.put("packet_loss_up", (int) (100f * ((float) (outPackets.total - outPackets.arrived) / (float) outPackets.total)));
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
    }
}
