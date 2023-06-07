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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class NioUdpMultiClientServer extends AbstractUdpServer<DatagramChannel> {

    private static final Logger logger = LoggerFactory.getLogger(NioUdpMultiClientServer.class);

    public final static int BUFFER_LENGTH = 1024;
    private final static String TAG = NioUdpMultiClientServer.class.getCanonicalName();
    final DatagramChannel channel;
    final InetAddress address;
    final int port;
    private final AtomicBoolean isRunning;
    private final String name;

    public NioUdpMultiClientServer(int port, InetAddress address) throws Exception {
        super(DatagramChannel.class);
        logger.info("Initializing " + TAG + " on " + address + ":" + port, 1, TestServerServiceEnum.TEST_SERVER);
        //this.socket = new DatagramSocket(port, TestServer.serverPreferences.getInetAddrBindTo());
        this.channel = TestServer.createDatagramChannel(port, address);
        this.isRunning = new AtomicBoolean(false);
        this.address = address;
        this.port = port;
        this.name = "NioUdpMultiClientServer [" + address + ":" + port + "]";
    }

    @Override
    public void run() {
        isRunning.set(true);

        try {
            logger.info("Starting " + TAG + " on address: " + channel.getLocalAddress() + " ...");

            channel.configureBlocking(false);

            final Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);

            while (isRunning.get()) {

                selector.select(5000);
                Set<SelectionKey> readyKeys = selector.selectedKeys();

                if (!readyKeys.isEmpty()) {
                    Iterator<SelectionKey> iterator = readyKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (key.isReadable() && key.isValid()) {
                            ((Buffer) buffer).clear();
                            SocketAddress senderAddr = channel.receive(buffer);
                            buffer.flip();
                            final byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);
                            final DatagramPacket dp = new DatagramPacket(data, data.length, senderAddr);
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
                                        + " (on local port :" + ((InetSocketAddress) channel.getLocalAddress()).getPort() + ") , #" + packetNumber + " TimeStamp: " + timeStamp + ", containing: " + clientUuid);

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
                                                clientData.getOnUdpPacketReceivedCallback().onReceive(dp, uuid, NioUdpMultiClientServer.this);
                                            }
                                        };

                                        TestServer.getCommonThreadPool().submit(onReceiveRunnable);
                                    }
                                }
                            }
                        }
                    }
                }


            }
        } catch (IOException e) {
            logger.error(getName(), e, 0, TestServerServiceEnum.UDP_SERVICE);
        } finally {
            if (channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.error(getName(), e, 2, TestServerServiceEnum.UDP_SERVICE);
                }
            }
        }

        logger.info("NioUdpMultiServer shutdown on address: " + address);
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    public void quit() {
        isRunning.set(false);
        try {
            logger.info("NioUdpServer received quit command on address: " + channel.getLocalAddress());
        } catch (IOException e) {
            logger.error(getName(), e, 1, TestServerServiceEnum.UDP_SERVICE);
        }
    }

    @Override
    public String toString() {
        return "NioUdpMultiClientServer [isRunning=" + isRunning + ", channel="
                + channel + ", address=" + address + ", incomingMap="
                + incomingMap + ", name=" + name + "]";
    }

    public String toStringExtra() {
        return " \n\t Socket additional info [local addr=" + address + ", connected="
                + channel.isConnected() + ", open=" + channel.isOpen() + "]";
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddress() {
        return address;
    }

    @Override
    public DatagramChannel getSocket() {
        return channel;
    }

    @Override
    public int getLocalPort() {
        return port;
    }

    @Override
    public void send(DatagramPacket dp) throws IOException {
        byte[] data = dp.getData();
        final ByteBuffer writeBuffer = ByteBuffer.allocate(data.length);
        ((Buffer) writeBuffer).clear();
        writeBuffer.put(dp.getData());
        ((Buffer) writeBuffer).flip();
        logger.info(getName() + " sending datagram: length = " + writeBuffer.array().length + ", to: " + dp.getSocketAddress());
        channel.send(writeBuffer, dp.getSocketAddress());
    }
}

