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
import at.alladin.rmbt.shared.qos.TcpResult;
import at.alladin.rmbt.shared.qos.UdpResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TcpUdpPortsReport implements AdvancedReport {

    public final static class PortsStats {
        public int total = 0;
        public int succeeded = 0;
    }

    @Override
    public void addSpeedtestReport(final Test test, final JSONObject testResultJson, final JSONObject reportJson) throws JSONException {
        //nothing to do with speed-test data
    }

    @Override
    public void addQoSReport(List<QoSTestResult> qosResultList, JSONObject reportJson) throws JSONException {
        final PortsStats tcpPorts = new PortsStats();
        final PortsStats udpPorts = new PortsStats();

        for (final QoSTestResult result : qosResultList) {
            if (result.getResult() instanceof TcpResult) {
                tcpPorts.total++;
                tcpPorts.succeeded += result.getFailureCounter() == 0 ? 1 : 0;
            } else if (result.getResult() instanceof UdpResult) {
                udpPorts.total++;
                udpPorts.succeeded += result.getFailureCounter() == 0 ? 1 : 0;
            }
        }

        reportJson.put("tcp_total", tcpPorts.total);
        reportJson.put("tcp_open", tcpPorts.succeeded);
        reportJson.put("udp_total", udpPorts.total);
        reportJson.put("udp_open", udpPorts.succeeded);
    }
}
