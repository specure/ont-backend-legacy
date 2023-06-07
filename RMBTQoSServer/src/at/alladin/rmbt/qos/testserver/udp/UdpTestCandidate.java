/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.alladin.rmbt.qos.testserver.udp;

import at.alladin.rmbt.qos.testserver.entity.TestCandidate;

import java.util.TreeSet;

public class UdpTestCandidate extends TestCandidate {
    public final static int TTL = 30000;

    private TreeSet<Integer> packetsReceived;
    private TreeSet<Integer> packetDuplicates;
    private int numPackets;
    private int remotePort;
    private boolean error;
    private String errorMsg;

    private UdpTestCompleteCallback onUdpTestCompleteCallback;
    private UdpPacketReceivedCallback onUdpPacketReceivedCallback;

    public UdpTestCandidate() {
        this.packetsReceived = new TreeSet<>();
        this.packetDuplicates = new TreeSet<>();
        this.error = false;
    }

    public TreeSet<Integer> getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(TreeSet<Integer> packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public TreeSet<Integer> getPacketDuplicates() {
        return packetDuplicates;
    }

    public void setPacketDuplicates(TreeSet<Integer> packetDuplicates) {
        this.packetDuplicates = packetDuplicates;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getNumPackets() {
        return numPackets;
    }

    public void setNumPackets(int numPackets) {
        this.numPackets = numPackets;
    }

    public UdpTestCompleteCallback getOnUdpTestCompleteCallback() {
        return onUdpTestCompleteCallback;
    }

    public void setOnUdpTestCompleteCallback(
            UdpTestCompleteCallback onUdpTestCompleteCallback) {
        this.onUdpTestCompleteCallback = onUdpTestCompleteCallback;
    }

    /**
     * @return
     */
    public UdpPacketReceivedCallback getOnUdpPacketReceivedCallback() {
        return onUdpPacketReceivedCallback;
    }

    /**
     * @param onUdpPacketReceivedCallback
     */
    public void setOnUdpPacketReceivedCallback(
            UdpPacketReceivedCallback onUdpPacketReceivedCallback) {
        this.onUdpPacketReceivedCallback = onUdpPacketReceivedCallback;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClientUdpData [packetsReceived=" + packetsReceived
                + ", numPackets=" + numPackets + ", remotePort=" + remotePort
                + ", error=" + error + ", errorMsg=" + errorMsg + "]";
    }
}
