/*******************************************************************************
 * Copyright 2013-2014 alladin-IT GmbH
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

import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.entity.ClientToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpSingleClientServer implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(UdpSingleClientServer.class);

    public final static int BUFFER_LENGTH = 1024;
    public final static byte UDP_IN_TEST_IDENTIFIER = 1;
    final DatagramSocket socket;

    final ClientToken token;

    final int numPackets;
    private final AtomicBoolean isRunning;
    private int receivedPackets = 0;


    public UdpSingleClientServer(int port, int numPackets, int timeOut, ClientToken token) throws SocketException {
        logger.info("Initializing UdpServer on port: " + port);
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(timeOut);
        this.isRunning = new AtomicBoolean(false);
        this.token = token;
        this.numPackets = numPackets;
    }

    public UdpSingleClientServer(DatagramSocket socket, int timeOut, ClientToken token) throws SocketException {
        logger.info("Starting UdpSingleClientServer on port: " + socket.getLocalPort());
        this.socket = socket;
        this.socket.setSoTimeout(timeOut);
        this.isRunning = new AtomicBoolean(false);
        this.token = token;
        this.numPackets = 0;
    }

    @Override
    public Integer call() {
        isRunning.set(true);

        Set<Integer> packetReceived = new TreeSet<>();

        try {
            while (isRunning.get()) {
                byte[] buffer = new byte[BUFFER_LENGTH];
                DatagramPacket dp = new DatagramPacket(buffer, BUFFER_LENGTH);

                socket.receive(dp);
                receivedPackets++;
                byte[] data = dp.getData();

                //check udp packet:
                if (data[0] != UDP_IN_TEST_IDENTIFIER) {
                    throw new IOException("bad UDP IN TEST identifier");
                }

                //check for duplicate packets:
                int packetNumber = data[1];
                if (packetReceived.contains(packetNumber)) {
                    throw new IOException("duplicate UDP IN TEST packet");
                }


                char[] uuid = new char[dp.getLength() - 2];
                for (int i = 2; i < dp.getLength(); i++) {
                    uuid[i - 2] = (char) data[i];
                }

                if (TestServer.serverPreferences.getVerboseLevel() >= 1) {
                    logger.info("received UDP from: " + dp.getAddress().toString() + ", #" + packetNumber + ", uuid: " + String.valueOf(uuid));
                }

                //received enough packets? exit loop
                if (receivedPackets >= numPackets) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (!socket.isClosed()) {
                socket.close();
            }
        }

        logger.info("UdpServer shutdown on port: " + socket.getPort());
        return new Integer(receivedPackets);
    }

    public boolean getIsRunning() {
        return isRunning.get();
    }

    public int getReceivedPacketsCount() {
        return receivedPackets;
    }

    public void quit() {
        isRunning.set(false);
        logger.info("UdpServer received quit command on port: " + socket.getPort());
    }
}
