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
package at.alladin.rmbt.qos.testserver.tcp;

import at.alladin.rmbt.qos.testserver.ClientHandler;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.TestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class TcpClientHandler implements Runnable {

    public final static int TCP_HANDLER_TIMEOUT = 10000;
    private static final Logger logger = LoggerFactory.getLogger(TcpClientHandler.class);
    private final Socket clientSocket;
    private final String name;

    private final AtomicReference<TcpMultiClientServer> tcpServer;

    public TcpClientHandler(Socket clientSocket, TcpMultiClientServer tcpServer) {
        this.clientSocket = clientSocket;
        this.tcpServer = new AtomicReference<TcpMultiClientServer>(tcpServer);
        this.name = "[TcpClientHandler " + clientSocket.getInetAddress().toString() + "]";
    }

    @Override
    public void run() {
        logger.info("New TCP ClientHander Thread started. Client: " + clientSocket);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             FilterOutputStream fos = new FilterOutputStream(clientSocket.getOutputStream());) {
            clientSocket.setSoTimeout(TCP_HANDLER_TIMEOUT);

            boolean validCandidate = false;

            if (TestServer.serverPreferences.isIpCheck()) {
                //check for test candidate if ip check is enabled
                if (!tcpServer.get().getCandidateMap().containsKey(clientSocket.getInetAddress())) {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }

                    logger.info(clientSocket.getInetAddress() + ": not a valid candidate for TCP/NTP",
                            TcpMultiClientServer.VERBOSE_LEVEL_REQUEST_RESPONSE, TestServerServiceEnum.TCP_SERVICE);
                } else {
                    validCandidate = true;
                }
            } else {
                //else allow connection
                validCandidate = true;
            }

            if (validCandidate) {
                tcpServer.get().refreshTtl(TcpMultiClientServer.TTL);

                //remove test candidate if ip check is enabled
                if (TestServer.serverPreferences.isIpCheck()) {
                    tcpServer.get().removeCandidate(clientSocket.getInetAddress());
                }

                String clientRequest = br.readLine();

                logger.info("TCP/NTP Server ({}" + tcpServer.get().getServerSocket() + ") (:" + tcpServer.get().getPort() + "), connection from: " + clientSocket.getInetAddress().toString() + ", request: " + clientRequest);

                // check request
                if (clientRequest != null) {
                    //send echo
                    byte[] response = ClientHandler.getBytesWithNewline(clientRequest);

                    logger.info("TCP/NTP Server (" + tcpServer.get().getServerSocket() + ") (:" + tcpServer.get().getPort() + "), response: " + new String(response) + " to: " + clientSocket.getInetAddress().toString());

                    fos.write(response);
                }
            }
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
        } catch (SocketTimeoutException e) {
            //logger.error("TcpClientHandler Thread {}", clientSocket.toString());
            logger.warn(e.getMessage());
            logger.warn("TcpClientHandler Thread {}", clientSocket != null ? clientSocket.toString() : "SocketTimeoutException");
        } catch (Exception e) {
            //logger.error("TcpClientHandler Thread {}", clientSocket.toString());
            logger.warn(e.getMessage());
            logger.warn("TcpClientHandler Thread {}", clientSocket != null ? clientSocket.toString() : "Exception");
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
