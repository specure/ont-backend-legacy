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

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpVersion;
import at.alladin.rmbt.util.net.rtp.RtpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpMultiClientServer extends AbstractUdpServer<DatagramSocket> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UdpMultiClientServer.class);
    public final static int BUFFER_LENGTH = 1024;
    private final static String TAG = UdpMultiClientServer.class.getCanonicalName();
    protected final DatagramSocket socket;
    final InetAddress address;

    final int port;
    private final AtomicBoolean isRunning;
    private final String name;

    public UdpMultiClientServer(int port, InetAddress address) throws Exception {
        super(DatagramSocket.class);
        logger.info("Initializing " + TAG + " on " + address + ":" + port);
        //this.socket = new DatagramSocket(port, TestServer.serverPreferences.getInetAddrBindTo());
        this.socket = TestServer.createDatagramSocket(port, address);
        this.port = port;
        this.isRunning = new AtomicBoolean(false);
        this.address = address;
        this.name = "UdpMultiClientServer [" + address + ":" + port + "]";
    }

    public UdpMultiClientServer(DatagramSocket socket) {
        super(DatagramSocket.class);
        this.socket = socket;
        this.port = socket.getLocalPort();
        this.address = socket.getLocalAddress();
        this.isRunning = new AtomicBoolean(false);
        this.name = "UdpMultiClientServer [" + address + ":" + socket.getLocalPort() + "]";
    }

    @Override
    public void run() {
        isRunning.set(true);

        logger.info("Starting " + TAG + " on address: " + socket.getLocalAddress() + ":" + socket.getLocalPort() + " ...");

        try {
            while (isRunning.get()) {
                byte[] buffer = new byte[BUFFER_LENGTH];
                final DatagramPacket dp = new DatagramPacket(buffer, BUFFER_LENGTH);
                socket.receive(dp);

                final byte[] data = dp.getData();

                final RtpVersion rtpVersion = RtpUtil.getVersion(data[0]);

                String clientUuid = null;

                if (!RtpVersion.VER2.equals(rtpVersion)) {
                    //Non RTP packet:
                    final int packetNumber = data[1];

                    String timeStamp = null;

                    try {
                        char[] uuid = new char[36];

                        for (int i = 2; i < 38; i++) {
                            uuid[i - 2] = (char) data[i];
                        }
                        clientUuid = String.valueOf(uuid);

                        char[] ts = new char[dp.getLength() - 38];
                        for (int i = 38; i < dp.getLength(); i++) {
                            ts[i - 38] = (char) data[i];
                        }

                        timeStamp = String.valueOf(ts);

                    } catch (Exception e) {
                        logger.error(getName(), e, 1, TestServerServiceEnum.UDP_SERVICE);
                    }

                    logger.info("received UDP from: " + dp.getAddress().toString() + ":" + dp.getPort()
                            + " (on local port :" + socket.getLocalPort() + ") , #" + packetNumber + " TimeStamp: " + timeStamp + ", containing: " + clientUuid);

                } else {
                    //RtpPacket received:
                    clientUuid = "VOIP_" + RtpUtil.getSsrc(data);
                }

                if (clientUuid != null) {
                    synchronized (incomingMap) {
                        final UdpTestCandidate clientData;
                        final String uuid = clientUuid;
                        if (!incomingMap.containsKey(clientUuid)) {
                            clientData = new UdpTestCandidate();
                            clientData.setNumPackets(Integer.MAX_VALUE);
                            clientData.setRemotePort(dp.getPort());
                            incomingMap.put(clientUuid, clientData);
                        } else {
                            clientData = (UdpTestCandidate) incomingMap.get(clientUuid);
                            if (clientData.isError()) {
                                continue;
                            }
                        }

                        //if a callback has been provided by the clienthandler run it in the background:
                        if (clientData.getOnUdpPacketReceivedCallback() != null) {
                            Runnable onReceiveRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    clientData.getOnUdpPacketReceivedCallback().onReceive(dp, uuid, UdpMultiClientServer.this);
                                }
                            };

                            TestServer.getCommonThreadPool().submit(onReceiveRunnable);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error(getName(), e, 0, TestServerServiceEnum.UDP_SERVICE);
        } finally {
            if (!socket.isClosed()) {
                socket.close();
            }
        }

        logger.info("UdpMultiServer shutdown on port: " + socket.getLocalPort());
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    public void quit() {
        isRunning.set(false);
        logger.info("UdpServer received quit command on port: " + socket.getLocalPort());
    }

    @Override
    public String toString() {
        return "UdpMultiClientServer [isRunning=" + isRunning + ", socket="
                + socket + ", address=" + address + ", incomingMap="
                + incomingMap + "]" + toStringExtra();
    }

    public String toStringExtra() {
        return " \n\t Socket additional info [local port=" + socket.getLocalPort() + ", connected="
                + socket.isConnected() + ", bound=" + socket.isBound() + ", closed=" + socket.isClosed() + "]";
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getLocalPort() {
        return port;
    }

    @Override
    public DatagramSocket getSocket() {
        return socket;
    }

    @Override
    public void send(DatagramPacket dp) throws IOException {
        socket.send(dp);
    }
}
