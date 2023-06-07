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
package at.alladin.rmbt.qos.testserver;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.entity.ClientToken;
import at.alladin.rmbt.qos.testserver.udp.*;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.PayloadType;
import at.alladin.rmbt.util.net.rtp.RealtimeTransportProtocol.RtpException;
import at.alladin.rmbt.util.net.rtp.RtpPacket;
import at.alladin.rmbt.util.net.rtp.RtpUtil;
import at.alladin.rmbt.util.net.rtp.RtpUtil.RtpQoSResult;
import org.postgresql.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * handles all client requests
 *
 * @author lb
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    public final static boolean ABORT_ON_DUPLICATE_UDP_PACKETS = false;
    public final static Pattern ID_REGEX_PATTERN = Pattern.compile("\\+ID([\\d]*)");
    public final static Pattern TOKEN_REGEX_PATTERN = Pattern.compile("TOKEN ([\\d\\w-]*)_([\\d]*)_(.*)");
    public final static boolean CHECK_TOKEN = false;
    protected final FilterInputStream in;
    protected final FilterOutputStream out;
    protected final BufferedReader reader;
    protected final String name;
    private final ServerSocket serverSocket;
    private final Socket socket;
    protected ConcurrentHashMap<Integer, UdpTestCandidate> clientUdpOutDataMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Integer, UdpTestCandidate> clientUdpInDataMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Integer, VoipTestCandidate> clientVoipDataMap = new ConcurrentHashMap<>();
    protected String clientProtocolVersion = QoSServiceProtocol.PROTOCOL_VERSION_1;

    public ClientHandler(ServerSocket serverSocket, Socket socket) throws IOException {
        this.serverSocket = serverSocket;
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new FilterOutputStream(socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.name = "[ClientHandler " + socket.getInetAddress().toString() + "]";
    }

    public synchronized static byte[] getBytesWithNewline(String string) {
        if (string.endsWith("\n")) {
            return getBytesWithNewline(string, false);
        } else {
            return getBytesWithNewline(string, true);
        }
    }

    public synchronized static byte[] getBytesWithNewline(String string, boolean appendNewLine) {
        if (appendNewLine) {
            return (string + "\n").getBytes();
        } else {
            return string.getBytes();
        }
    }

    private static String calculateHMAC(final String secret, final String data) {
        try {
            final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            final byte[] rawHmac = mac.doFinal(data.getBytes());
            final String result = new String(Base64.encodeBytes(rawHmac));
            return result;
        } catch (final GeneralSecurityException e) {
            logger.info("Unexpected error while creating hash: " + e.getMessage());
            return "";
        }
    }

    @Override
    public void run() {
        logger.info("New connection from: " + socket.getInetAddress().toString());
        String message;
        String command = null;

        try {
            socket.setSoTimeout(QoSServiceProtocol.TIMEOUT_CLIENTHANDLER_CONNECTION_MIN_VALUE);

            out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_GREETING));
            out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_ACCEPT_TOKEN));
            message = reader.readLine();
            logger.info("GOT: " + message);

            ClientToken token = checkToken(message);

            logger.info("TOKEN OK");

            out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_OK));
            out.write(getBytesWithNewline(QoSServiceProtocol.RESPONSE_ACCEPT_COMMANDS));

            boolean quit = false;

            TestServer.clientHandlerSet.add(this);

            while (!quit) {
                try {
                    command = reader.readLine();
                    logger.info("COMMAND: " + command + " from: " + socket.getInetAddress().toString());
                    if (command != null) {
                        if (command.startsWith(QoSServiceProtocol.CMD_NON_TRANSPARENT_PROXY_TEXT)) {
                            runNonTransparentProxyTest(command);
                        } else if (command.startsWith(QoSServiceProtocol.CMD_TCP_TEST_IN)) {
                            runIncomingTcpTest(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.CMD_TCP_TEST_OUT)) {
                            runOutgoingTcpTest(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.CMD_UDP_TEST_OUT)) {
                            runOutgoingUdpTest(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.CMD_UDP_TEST_IN)) {
                            runIncomingUdpTest(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.CMD_VOIP_TEST)) {
                            runVoipTest(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_PORT_RANGE)) {
                            sendCommand(TestServer.serverPreferences.getUdpPortMin() + " " + TestServer.serverPreferences.getUdpPortMax(), command);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_PORT)) {
                            sendRandomUdpPort(command);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_RESULT_OUT)) {
                            runRcvCommand(command, token, false);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_UDP_RESULT_IN)) {
                            runRcvCommand(command, token, true);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_VOIP_RESULT)) {
                            runVoipResultCommand(command, token);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_QUIT)) {
                            quit = true;
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_NEW_CONNECTION_TIMEOUT)) {
                            requestNewConnectionTimeout(command);
                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_PROTOCOL_VERSION)) {

                        } else if (command.startsWith(QoSServiceProtocol.REQUEST_PROTOCOL_KEEPALIVE)) {

                        } else {
                            sendCommand(QoSServiceProtocol.RESPONSE_ACCEPT_COMMANDS, command);
                            quit = true;
                        }
                    } else {
                        quit = true;
                    }
                } catch (Exception e) {
                    logger.error("ClientHandler: " + socket.getInetAddress().toString()
                            + (command == null ? " [No command submitted]" : " [Command: " + command + "] - Exception catched and consumed."));
                    throw e;
                }
            }
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
        } catch (SSLHandshakeException e) {
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.warn(name, e, 0, TestServerServiceEnum.TEST_SERVER);
        } finally {
            TestServer.clientHandlerSet.remove(this);
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    logger.info(name + " Connection closed!");
                } catch (Exception e) {
                    logger.error(name + " Could not close socket!");
                }
            }
        }
    }

    private ClientToken checkToken(String token) throws IOException {
        ClientToken clientToken;

        try {
            logger.info("Got token: " + token);
            Matcher m = TOKEN_REGEX_PATTERN.matcher(token);
            m.find();

            if (m.groupCount() != 3) {
                throw new IOException("BAD TOKEN: Bad Arguments!\n");
            } else {
                String uuid = m.group(1);
                long timeStamp = Long.parseLong(m.group(2));
                String hmac = m.group(3);

                if (CHECK_TOKEN) {
                    String controlHmac = calculateHMAC(TestServer.serverPreferences.getSecretKey(), uuid + "_" + timeStamp);
                    if (controlHmac.equals(hmac) && (timeStamp + QoSServiceProtocol.TOKEN_LEGAL_TIME >= System.currentTimeMillis())) {
                        clientToken = new ClientToken(uuid, timeStamp, hmac);
                        return clientToken;
                    } else {
                        throw new IOException("BAD TOKEN. Bad Key!\n" + controlHmac + " <-> " + hmac + "\n");
                    }
                } else {
                    return new ClientToken(uuid, timeStamp, hmac);
                }
            }
        } catch (IOException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Exception! {}", e.getMessage());
            throw new IOException("BAD TOKEN: " + token);
        }
    }

    private void sendRandomUdpPort(final String command) throws IOException {
        int randomPort = 0;
        Random rand = new Random();
        if ((TestServer.serverPreferences.getUdpPortMax() > 0) && (TestServer.serverPreferences.getUdpPortMin() <= TestServer.serverPreferences.getUdpPortMax())) {
            randomPort = rand.nextInt(TestServer.serverPreferences.getUdpPortMax() - TestServer.serverPreferences.getUdpPortMin()) +
                    TestServer.serverPreferences.getUdpPortMin();

        }
        logger.info("Requested UDP Port. Picked random port number: " + randomPort);
        sendCommand(String.valueOf(randomPort), command);
    }

    private void runIncomingTcpTest(String command, ClientToken token) throws IOException {
        logger.info("runIncomingTcpTest...");
        final int port;

        Pattern p = Pattern.compile(QoSServiceProtocol.CMD_TCP_TEST_IN + " ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();
        if (m.groupCount() != 1) {
            logger.error("tcp incoming test command syntax error: " + command);
            throw new IOException("tcp incoming test command syntax error: " + command);
        } else {
            port = Integer.parseInt(m.group(1));
        }

        Runnable tcpInRunnable = new Runnable() {

            @Override
            public void run() {
                Socket testSocket = null;
                try {
                    testSocket = new Socket(socket.getInetAddress(), port);
                    BufferedOutputStream out = new BufferedOutputStream(testSocket.getOutputStream());
                    out.write(getBytesWithNewline("HELLO TO " + port));
                    out.flush();
                    testSocket.close();
                } catch (Exception e) {
                    logger.error(name, e, 2, TestServerServiceEnum.TCP_SERVICE);
                } finally {
                    if (testSocket != null && !testSocket.isClosed()) {
                        try {
                            testSocket.close();
                        } catch (IOException e) {
                            logger.error("Exception! {}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                logger.info("runIncomingTcpTest... DONE");
            }
        };

        TestServer.getCommonThreadPool().execute(tcpInRunnable);
    }

    private void runOutgoingTcpTest(String command, ClientToken token) throws Exception {
        logger.info("runOutgoingTcpTest...");
        int port;

        Pattern p = Pattern.compile(QoSServiceProtocol.CMD_TCP_TEST_OUT + " ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();
        if (m.groupCount() != 1) {
            logger.error("tcp outgoing test command syntax error: " + command);
            throw new IOException("tcp outgoing test command syntax error: " + command);
        } else {
            port = Integer.parseInt(m.group(1));
        }

        try {
            TestServer.registerTcpCandidate(port, socket);

            sendCommand(QoSServiceProtocol.RESPONSE_OK, command);
            logger.info("runOutgoingTcpTest... DONE");
        } catch (Exception e) {
            logger.error(name + (command == null ?
                    " [No command submitted]" : " [Command: " + command + "]"));
        } finally {
            //is beeing done inside TcpServer now:
            //tcpServer.removeCandidate(socket.getInetAddress());
        }
    }

    private void runIncomingUdpTest(final String command, final ClientToken token) throws IOException, InterruptedException {
        final int port;
        final int timeout = 5000;
        final int numPackets;

        Pattern p = Pattern.compile(QoSServiceProtocol.CMD_UDP_TEST_IN + " ([\\d]*) ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();
        if (m.groupCount() != 2) {
            logger.error("udp incoming test command syntax error: " + command);
            throw new IOException("udp incoming test command syntax error: " + command);
        } else {
            port = Integer.parseInt(m.group(1));
            numPackets = Integer.parseInt(m.group(2));
        }

        //DatagramSocket sock = new DatagramSocket(port);
        final UdpTestCandidate clientData = new UdpTestCandidate();
        clientData.setNumPackets(numPackets);
        final DatagramSocket sock = new DatagramSocket();

        final CountDownLatch latch = new CountDownLatch(1);
        final Runnable sendUdpPacketsRunnable = new Runnable() {

            @Override
            public void run() {
                sendUdpPackets(socket.getInetAddress(), sock, port, 3000, numPackets, true, 100, token, clientData);
                latch.countDown();
            }
        };

        TestServer.getCommonThreadPool().execute(sendUdpPacketsRunnable);

        final Matcher idMatcher = ID_REGEX_PATTERN.matcher(command);
        if (!idMatcher.find()) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
            sendRcvResult(clientData, port, command);
        }
    }

    private UdpTestCandidate sendUdpPackets(InetAddress targetHost, DatagramSocket sock, int port, int timeOut,
                                            int numPackets, boolean awaitResponse, int delay, ClientToken token, final UdpTestCandidate clientData) {
        clientUdpInDataMap.put(port, clientData);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);

        logger.info("INIT sending UDP packets (amount = " + numPackets + ") to " + targetHost + " on port " + port
                + " - using DatagramSocket: " + sock.getLocalAddress() + ":" + sock.getLocalPort());


        try {
            sock.setSoTimeout(timeOut);
        } catch (SocketException e) {
            logger.error(e.getMessage());
            return clientData;
        }

        byte[] data;

        for (int i = 0; i < numPackets; i++) {

            byteOut.reset();
            try {
                Thread.sleep(delay);
                dataOut.writeByte(QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER);
                dataOut.writeByte(i);
                dataOut.write(token.getUuid().getBytes());
            } catch (IOException | InterruptedException e) {
                logger.error(e.getMessage());
                sock.close();
                return clientData;
            }

            try {
                byteOut.flush();
                data = byteOut.toByteArray();

                DatagramPacket packet = new DatagramPacket(data, data.length, targetHost, port);
                sock.send(packet);

                if (awaitResponse) {
                    try {
                        byte buffer[] = new byte[1024];

                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                        sock.receive(dp);

                        int packetNumber = buffer[1];

                        logger.info(dp.getAddress() + ": UDP Test received packet: #" + packetNumber + " -> " + buffer,
                                2, TestServerServiceEnum.UDP_SERVICE);

                        //check udp packet:
                        if (buffer[0] != QoSServiceProtocol.UDP_TEST_RESPONSE) {
                            logger.info(dp.getAddress() + ": bad UDP IN TEST packet identifier", 0, TestServerServiceEnum.UDP_SERVICE);
                            throw new IOException("bad UDP IN TEST packet identifier");
                        }

                        //check for duplicate packets:
                        if (clientData.getPacketsReceived().contains(packetNumber)) {
                            logger.info(dp.getAddress() + ": duplicate UDP IN TEST packet id", 0, TestServerServiceEnum.UDP_SERVICE);
                            clientData.getPacketDuplicates().add(packetNumber);
                            if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
                                throw new IOException("duplicate UDP IN TEST packet id");
                            }
                        } else {
                            clientData.getPacketsReceived().add(new Integer(packetNumber));
                        }
                    } catch (SocketTimeoutException e) {
                        logger.error("Packet not received! Exception: {}", e.getMessage());
                        //packet not received
                    }
                }
            } catch (IOException e) {
                logger.error("Exception: {}", e.getMessage());
                sock.close();
                return clientData;
            }

            logger.info("Sent packet pnum:" + i + " to " + targetHost + ":" + port + ", sent message:" + data,
                    2, TestServerServiceEnum.TEST_SERVER);
        }

        try {
            byteOut.close();
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
            return clientData;
        } finally {
            sock.close();
        }

        logger.info(socket.getInetAddress() + ": Udp Incoming finished! RCV PACKETS: " + clientData.getPacketsReceived().size()
                + ", DUP: " + clientData.getPacketDuplicates().size(), 2, TestServerServiceEnum.UDP_SERVICE);

        return clientData;
    }

    private void runOutgoingUdpTest(final String command, final ClientToken token) throws IOException, InterruptedException {
        final long timeout = 5000;
        final Pattern p = Pattern.compile(QoSServiceProtocol.CMD_UDP_TEST_OUT + " ([\\d]*) ([\\d]*)");
        final Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 2) {
            logger.error("udp outgoing test command syntax error: " + command);
            throw new IOException("udp outgoing test command syntax error: " + command);
        }


        final int port = Integer.parseInt(m.group(1));
        final int numPackets = Integer.parseInt(m.group(2));

        logger.info("Starting UDP OUT TEST (requested packets: " + numPackets + ") on port :" + port + " for " + socket.getInetAddress().toString());

        final UdpTestCandidate udpData = new UdpTestCandidate();
        udpData.setNumPackets(numPackets);

        clientUdpOutDataMap.put(port, udpData);

        final UdpPacketReceivedCallback receiveCallback = new UdpPacketReceivedCallback() {

            @Override
            public boolean onReceive(final DatagramPacket dp, final String uuid, final AbstractUdpServer<?> udpServer) {
                final byte[] data = dp.getData();
                final int packetNumber = data[1];
                final UdpTestCandidate clientUdpData = udpServer.getClientData(uuid);

                //check udp packet:
                if (data[0] != QoSServiceProtocol.UDP_TEST_ONE_DIRECTION_IDENTIFIER && data[0] != QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
                    logger.info(dp.getAddress() + ": bad UDP IN TEST packet identifier", 0, TestServerServiceEnum.UDP_SERVICE);
                    clientUdpData.setError(true);
                    clientUdpData.setErrorMsg("bad UDP IN TEST packet identifier");
                }

                //check for duplicate packets:
                if (clientUdpData.getPacketsReceived().contains(packetNumber)) {
                    //DUP
                    logger.info(dp.getAddress() + ": duplicate UDP IN TEST packet id", 0, TestServerServiceEnum.UDP_SERVICE);
                    clientUdpData.getPacketDuplicates().add(packetNumber);
                    if (ABORT_ON_DUPLICATE_UDP_PACKETS) {
                        clientUdpData.setError(true);
                        clientUdpData.setErrorMsg("duplicate UDP IN TEST packet id");
                    }
                } else {
                    //regular packet received:
                    clientUdpData.getPacketsReceived().add(new Integer(packetNumber));

                    if (data[0] == QoSServiceProtocol.UDP_TEST_AWAIT_RESPONSE_IDENTIFIER) {
                        data[0] = QoSServiceProtocol.UDP_TEST_RESPONSE;
                        DatagramPacket response = new DatagramPacket(data, dp.getLength(), dp.getAddress(), dp.getPort());
                        try {
                            udpServer.send(response);
                        } catch (Exception e) {
                            logger.error("Exception! {}", e.getMessage());
                            //ignore exception (can be a blocked outgoing port; in this case the test should continue normally)
                        }
                    }

                    logger.info(name + " received regular Packet #" + packetNumber, 2, TestServerServiceEnum.UDP_SERVICE);

                    //if all packets have been received and an onComplete callback exists run it and remove the client data
                    //from the udp server
                    if (clientUdpData.getPacketsReceived().size() >= clientUdpData.getNumPackets()
                            && clientUdpData.getOnUdpTestCompleteCallback() != null) {
                        if (clientUdpData.getOnUdpTestCompleteCallback().onComplete(udpServer)) {
                            udpServer.pollClientData(token.getUuid());
                        }
                    }

                    return true;
                }

                return false;
            }
        };

        //packet receive callback
        udpData.setOnUdpPacketReceivedCallback(receiveCallback);

        final CountDownLatch latch = new CountDownLatch(1);

        final UdpTestCompleteCallback finishCallback = new UdpTestCompleteCallback() {

            @Override
            public boolean onComplete(final AbstractUdpServer<?> udpServer) {
                try {
                    logger.info("UDP OUT TEST on port :" + port + " for " + socket.getInetAddress().toString() + ":" + socket.getPort()
                            + " finished successfully...");
                    latch.countDown();
                    return true;
                } catch (Exception e) {
                    logger.error("Exception! {}", e.getMessage());
                }

                return false;
            }
        };

        //add callback to udp client data in case all udp packets will arrive. in this case we can send back "RCV" before the
        //final timeout is reached
        udpData.setOnUdpTestCompleteCallback(finishCallback);

        //register udp client data
        TestServer.registerUdpCandidate(socket.getLocalAddress(), port, token.getUuid(), udpData);

        //tell the client that we are ready
        try {
            sendCommand(QoSServiceProtocol.RESPONSE_OK, command);
        } catch (IOException e) {
            logger.error("Exception! {}", e.getMessage());
        }

        final Matcher idMatcher = ID_REGEX_PATTERN.matcher(command);
        if (!idMatcher.find()) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
            sendRcvResult(clientUdpOutDataMap.get(port), port, command);
        }
    }

    private void runVoipTest(final String command, final ClientToken token) throws IOException, InterruptedException {
        /*
         * syntax: VOIPTEST 0 1 2 3 4 5 6 7
         * 	0 = outgoing port (server port)
         * 	1 = incoming port (client port)
         *  2 = sample rate (in Hz)
         * 	3 = bits per sample
         * 	4 = packet delay in ms
         * 	5 = call duration (test duration) in ms
         * 	6 = starting sequence number (see rfc3550, rtp header: sequence number)
         *  7 = payload type
         */
        final Pattern p = Pattern.compile(QoSServiceProtocol.CMD_VOIP_TEST + " ([\\d]*) ([\\d]*) ([\\d]*) ([\\d]*) ([\\d]*) ([\\d]*) ([\\d]*) ([\\d]*)");
        final Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 8) {
            throw new IOException("voip test command syntax error: " + command);
        }

        final int portOut = Integer.parseInt(m.group(1));
        final int portIn = Integer.parseInt(m.group(2));
        final int sampleRate = Integer.parseInt(m.group(3));
        final int bps = Integer.parseInt(m.group(4));
        final int delay = Integer.parseInt(m.group(5));
        final int callDuration = Integer.parseInt(m.group(6));
        final long sequenceNumber = Integer.parseInt(m.group(7));
        final int payloadTypeValue = Integer.parseInt(m.group(8));
        final int ssrc = TestServer.randomizer.next();

        logger.info("Starting VOIP TEST (sample rate: " + sampleRate + ", bps: " + bps + ", delay: " + delay
                + ", call duration: " + callDuration + ", ssrc: " + ssrc + ", seq number: " + sequenceNumber
                + ") on outgoing port :" + portOut + "/incoming port: " + portIn + " for " + socket.getInetAddress().toString());

        final VoipTestCandidate voipData = new VoipTestCandidate(sequenceNumber, sampleRate);

        clientVoipDataMap.put(ssrc, voipData);

        final UdpPacketReceivedCallback receiveCallback = new UdpPacketReceivedCallback() {

            @Override
            public boolean onReceive(final DatagramPacket dp, final String uuid, final AbstractUdpServer<?> udpServer) {
                final long timestampNs = System.nanoTime();
                final byte[] data = dp.getData();
                final VoipTestCandidate clientVoipData = (VoipTestCandidate) udpServer.getClientData(uuid);

                try {
                    if (clientVoipData.getRtpControlDataList().size() == 0) {
                        final InetAddress targetAddr = dp.getAddress();
                        final int targetPort = dp.getPort();

                        logger.info(getName() + " Voip: Received first packet! Starting response stream for: " + targetAddr.toString() + ":" + targetPort);
                        final Runnable rtpStreamSendRunnable = new Runnable() {

                            @Override
                            public void run() {
                                PayloadType payloadType = PayloadType.getByCodecValue(payloadTypeValue);
                                payloadType = PayloadType.UNKNOWN.equals(payloadType) ? PayloadType.PCMA : payloadType;
                                try {
                                    RtpUtil.runVoipStream(udpServer.getSocket(), false, targetAddr, targetPort, sampleRate, bps,
                                            payloadType, sequenceNumber, ssrc, callDuration, delay, 10000, true, null);
                                } catch (Exception e) {
                                    logger.error(getName(), e, 0, TestServerServiceEnum.UDP_SERVICE);
                                }
                            }
                        };

                        TestServer.getCommonThreadPool().execute(rtpStreamSendRunnable);
                    }

                    RtpPacket rtpPacket = new RtpPacket(data);
                    logger.info(getName() + " RTP Packet received. Sequence Number: "
                            + rtpPacket.getSequnceNumber() + ", TS: " + timestampNs + ", SSRC: " + rtpPacket.getSsrc());
                    clientVoipData.resetTtl(3000);
                    clientVoipData.addRtpControlData(rtpPacket, timestampNs);
                } catch (RtpException e) {
                    logger.error(getName(), e, 1, TestServerServiceEnum.UDP_SERVICE);
                    return true;
                }
                //check udp packet:
                return true;
            }
        };

        //packet receive callback
        voipData.setOnUdpPacketReceivedCallback(receiveCallback);

        //register udp client data
        TestServer.registerUdpCandidate(socket.getLocalAddress(), portOut, "VOIP_" + String.valueOf(ssrc), voipData);

        //tell the client that we are ready
        try {
            sendCommand(QoSServiceProtocol.RESPONSE_OK + " " + String.valueOf(ssrc), command);
        } catch (IOException e) {
            logger.error(getName(), e, 0, TestServerServiceEnum.UDP_SERVICE);
        }
    }

    /**
     * @param command
     * @param token
     * @throws IOException
     */
    private void runRcvCommand(String command, ClientToken token, boolean isIncoming) throws IOException {
        Pattern p = Pattern.compile((isIncoming ? QoSServiceProtocol.REQUEST_UDP_RESULT_IN : QoSServiceProtocol.REQUEST_UDP_RESULT_OUT) + " ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 1) {
            logger.error("RCV command syntax error: " + command);
            throw new IOException("RCV command syntax error: " + command);
        } else {
            final int port = Integer.parseInt(m.group(1));
            sendRcvResult(isIncoming ? clientUdpInDataMap.get(port) : clientUdpOutDataMap.get(port), port, command);
        }
    }


    private void sendRcvResult(final UdpTestCandidate result, final int port, final String command) throws IOException {
        if (result != null && result.getPacketsReceived() != null && !result.isError()) {
            logger.info("RESULT OK, RCV PACKETS: " + result.getPacketsReceived().size() + ", DUP: " + result.getPacketDuplicates().size());
            sendCommand(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " " + result.getPacketsReceived().size() + " " + result.getPacketDuplicates().size(), command);
        } else {
            logger.info("RESULT ERROR, error: " + (result != null ? result.getErrorMsg() : "sorry, no error message available!"),
                    1, TestServerServiceEnum.UDP_SERVICE);
            sendCommand(QoSServiceProtocol.RESPONSE_UDP_NUM_PACKETS_RECEIVED + " 0 0", command);
        }
    }

    private void runVoipResultCommand(String command, ClientToken token) throws IOException {
        Pattern p = Pattern.compile(QoSServiceProtocol.REQUEST_VOIP_RESULT + " ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 1) {
            throw new IOException("GET VOIPRESULT command syntax error: " + command);
        } else {
            final int ssrc = Integer.parseInt(m.group(1));
            sendVoipResult(command, token, ssrc);
        }
    }

    private void sendVoipResult(String command, ClientToken token, int ssrc) throws IOException {
        System.out.println("VOIP RESULT FOR " + ssrc);
        final VoipTestCandidate voipTc = clientVoipDataMap.get(ssrc);
        if (voipTc != null) {
            try {
                RtpQoSResult result = RtpUtil.calculateQoS(voipTc.getRtpControlDataList(),
                        voipTc.getInitialSequenceNumber(), voipTc.getSampleRate());
                System.out.println(result);
                sendCommand(QoSServiceProtocol.RESPONSE_VOIP_RESULT + " " + result.getMaxJitter() + " "
                        + result.getMeanJitter() + " " + result.getMaxDelta() + " " + result.getSkew() + " "
                        + result.getReceivedPackets() + " " + result.getOutOfOrder() + " "
                        + result.getMinSequential() + " " + result.getMaxSequencial(), command);
            } catch (ArithmeticException e) {
                logger.warn(e.getMessage());
            } catch (Exception e) {
                logger.error(getName(), e, 1, TestServerServiceEnum.UDP_SERVICE);
            }
        } else {
            sendCommand(QoSServiceProtocol.RESPONSE_ERROR_ILLEGAL_ARGUMENT + " " + ssrc, command);
        }
    }


    private void runNonTransparentProxyTest(String command) throws Exception {
        int echoPort;

        Pattern p = Pattern.compile(QoSServiceProtocol.CMD_NON_TRANSPARENT_PROXY_TEXT + " ([\\d]*)");
        Matcher m = p.matcher(command);
        m.find();
        if (m.groupCount() != 1) {
            throw new IOException("non transparent proxy test command syntax error: " + command);
        } else {
            echoPort = Integer.parseInt(m.group(1));
        }

        try {
            TestServer.registerTcpCandidate(echoPort, socket);

            sendCommand(QoSServiceProtocol.RESPONSE_OK, command);
            logger.info("NTP: sendind OK. waiting for request...");
        } catch (Exception e) {
            logger.error(name, e, 1, TestServerServiceEnum.TCP_SERVICE);
        } finally {
            //is beeing done inside TcpServer now:
            //tcpServer.removeCandidate(socket.getInetAddress());
        }
    }

    public void requestNewConnectionTimeout(String command) throws IOException {
        final Pattern p = Pattern.compile(QoSServiceProtocol.REQUEST_NEW_CONNECTION_TIMEOUT + " ([\\d]*)");
        final Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 1) {
            throw new IOException("request new connection timeout command syntax error: " + command);
        }

        Integer requestedConnTimeout = Integer.parseInt(m.group(1));
        if (requestedConnTimeout < QoSServiceProtocol.TIMEOUT_CLIENTHANDLER_CONNECTION_MIN_VALUE) {
            sendErrorCommand(QoSServiceProtocol.RESPONSE_ERROR_ILLEGAL_ARGUMENT + " " + requestedConnTimeout, command);
        } else {
            socket.setSoTimeout(requestedConnTimeout);
            sendCommand(QoSServiceProtocol.RESPONSE_OK, command);
        }
    }

    public void requestProtocolVersion(String command) throws IOException {
        final Pattern p = Pattern.compile(QoSServiceProtocol.REQUEST_PROTOCOL_VERSION + " ([a-zA-Z0-9]*)");
        final Matcher m = p.matcher(command);
        m.find();

        if (m.groupCount() != 1) {
            throw new IOException("request protocol version command syntax error: " + command);
        }

        String requestedProtocolVersion = m.group(1);
        if (!QoSServiceProtocol.SUPPORTED_PROTOCOL_VERSION_SET.contains(requestedProtocolVersion)) {
            sendErrorCommand(QoSServiceProtocol.RESPONSE_ERROR_UNSUPPORTED + " " + requestedProtocolVersion, command);
        } else {
            clientProtocolVersion = requestedProtocolVersion;
            sendCommand(QoSServiceProtocol.RESPONSE_OK, command);
        }
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }

    public synchronized void sendCommand(String outCommand) throws IOException {
        sendCommand(outCommand, null);
    }

    public synchronized void sendCommand(String outCommand, String inCommand) throws IOException {
        String id = null;
        if (inCommand != null) {
            Matcher m = ID_REGEX_PATTERN.matcher(inCommand);
            if (m.find()) {
                id = m.group(1);
                outCommand = outCommand + (id != null ? " +ID" + id : "");
            }
        }

        logger.info(name + " sending answer: [" + outCommand + "] to: [" + inCommand + "] ");
        out.write(getBytesWithNewline(outCommand));
    }

    public synchronized void sendErrorCommand(String error) throws IOException {
        sendCommand(QoSServiceProtocol.RESPONSE_ERROR_RESPONSE + error);
    }

    public synchronized void sendErrorCommand(String error, String inCommand) throws IOException {
        sendCommand(QoSServiceProtocol.RESPONSE_ERROR_RESPONSE + error, inCommand);
    }

    @Override
    public String toString() {
        return "ClientHandler [socket=" + socket + ", name=" + name
                + ", clientUdpOutDataMap=" + clientUdpOutDataMap
                + ", clientUdpInDataMap=" + clientUdpInDataMap + "]";
    }

    public String getName() {
        return name;
    }

    public ConcurrentHashMap<Integer, UdpTestCandidate> getClientUdpOutDataMap() {
        return clientUdpOutDataMap;
    }

    public ConcurrentHashMap<Integer, UdpTestCandidate> getClientUdpInDataMap() {
        return clientUdpInDataMap;
    }
}
