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
package at.alladin.rmbt.qos.testserver.tcp;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import at.alladin.rmbt.qos.testserver.servers.AbstractTcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author lb
 */
public class TcpMultiClientServer extends AbstractTcpServer {

    private static final Logger logger = LoggerFactory.getLogger(TcpMultiClientServer.class);

    public final static long TTL = 30000;
    public final static int VERBOSE_LEVEL_REGISTER_REMOVE_CANDIDATE = 0;
    public final static int VERBOSE_LEVEL_REQUEST_RESPONSE = 0;
    public final static boolean CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT = true;
    public final static boolean CLOSE_CONNECTION_IF_TTL_REACHED = true;
    public final static int TIMEOUT = 20000;
    public final static boolean HAS_TIMEOUT = false;

    public TcpMultiClientServer(int port, InetAddress addr) throws IOException {
        super(addr, port);
    }

    @Override
    public synchronized void prepare() throws Exception {
        logger.info("Preparing TCP socket on port " + port + " for TCP/NTP test.", 2, TestServerServiceEnum.TCP_SERVICE);

        if (TestServer.serverPreferences.isIpCheck()) {
            if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
                currentConnections.addAndGet(1);
                logger.info("Socket on port " + port + " still opened. Candidate count has been increased by 1 (current count: "
                        + currentConnections.get() + ").", 2, TestServerServiceEnum.TCP_SERVICE);
            }
        }

        //check if this thread is alive
        boolean isThreadRunning = isAlive();

        //refresh the TTL
        refreshTtl(TTL);

        if (serverSocket == null) {
            serverSocket = TestServer.createServerSocket(getPort(), false, getInetAddr());
            logger.info(getName() + " has been (re)opened.", 2, TestServerServiceEnum.TCP_SERVICE);
        }

        if (serverSocket != null && HAS_TIMEOUT) {
            serverSocket.setSoTimeout(TIMEOUT);
        }

        //start thread if not running
        if (!isThreadRunning) {
            TestServer.getCommonThreadPool().submit(this);
        }
    }

    @Override
    public synchronized boolean close() throws IOException {
        if (TestServer.serverPreferences.isIpCheck()) {
            if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
                final long connectionsLeft = currentConnections.addAndGet(-1);
                if (this.serverSocket != null && !this.serverSocket.isClosed() && connectionsLeft <= 0) {
                    closeSocket();
                    logger.info("Closed socket on port " + port + "; Reason: empty candidate list.");
                    return true;
                }
            }
        } else {
            if (CLOSE_CONNECTION_IF_TTL_REACHED) {
                if (this.serverSocket != null && !this.serverSocket.isClosed() && System.currentTimeMillis() >= ttlTimestamp.get()) {
                    closeSocket();
                    logger.info("Closed socket on port " + port + "; Reason: TTL of " + TTL + "ms reached.");
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public synchronized boolean isAlive() {
        if (TestServer.serverPreferences.isIpCheck()) {
            if (CLOSE_CONNECTION_IF_NO_CLIENTS_LEFT) {
                final long connectionsLeft = currentConnections.get();
                return (this.serverSocket != null && !this.serverSocket.isClosed() && connectionsLeft > 0);
            }
        } else {
            if (CLOSE_CONNECTION_IF_TTL_REACHED) {
                return (this.serverSocket != null && !this.serverSocket.isClosed() && System.currentTimeMillis() < ttlTimestamp.get());
            }
        }

        return false;
    }

    @Override
    protected void execute() throws Exception {
        Socket clientSocket = serverSocket.accept();
        TcpClientHandler tcpClientHandler = new TcpClientHandler(clientSocket, this);
        TestServer.getCommonThreadPool().execute(tcpClientHandler);
    }

    @Override
    public String toString() {
        return "TcpMultiClientServer [candidateMap=" + candidateMap
                + ", currentConnections=" + currentConnections
                + ", serverSocketTypeClazz=" + serverSocketTypeClazz
                + ", clientHandlerRunnableClazz=" + clientDataHolderClazz
                + ", ttlTimestamp=" + ttlTimestamp + ", serverSocket="
                + serverSocket + ", port=" + port + ", inetAddr=" + inetAddr
                + "]";
    }
}
