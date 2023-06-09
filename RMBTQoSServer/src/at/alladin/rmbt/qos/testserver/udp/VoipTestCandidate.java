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
package at.alladin.rmbt.qos.testserver.udp;

import at.alladin.rmbt.util.net.rtp.RtpPacket;
import at.alladin.rmbt.util.net.rtp.RtpUtil.RtpControlData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoipTestCandidate extends UdpTestCandidate {

    private final Map<Integer, RtpControlData> rtpControlDataList = new ConcurrentHashMap<>();

    private final long initialSequenceNumber;
    private final int sampleRate;

    public VoipTestCandidate(long initialSequenceNumber, int sampleRate) {
        this.sampleRate = sampleRate;
        this.initialSequenceNumber = initialSequenceNumber;
    }

    public void addRtpControlData(RtpPacket rtpPacket, long recTimestampNs) {
        rtpControlDataList.put(rtpPacket.getSequnceNumber(), new RtpControlData(rtpPacket, recTimestampNs));
    }

    public Map<Integer, RtpControlData> getRtpControlDataList() {
        return rtpControlDataList;
    }

    public long getInitialSequenceNumber() {
        return initialSequenceNumber;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public String toString() {
        return "VoipTestCandidate [rtpControlDataList.size()=" + rtpControlDataList.size()
                + ", initialSequenceNumber=" + initialSequenceNumber
                + ", sampleRate=" + sampleRate + ", toString()="
                + super.toString() + "]";
    }
}
