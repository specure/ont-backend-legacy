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
package at.alladin.rmbt.controlServer.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;

/**
 * @author lb
 */
public class ProxyEchoServerSocket implements Callable<ProxyEchoResult> {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ProxyEchoServerSocket.class);

    private final ProxyEchoRequest request;

    /**
     * @param request
     */
    public ProxyEchoServerSocket(ProxyEchoRequest request) {
        this.request = request;
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public ProxyEchoResult call() throws Exception {
        //listen to the requested port:
        try (ServerSocket server = new ServerSocket(request.getPort())) {
            logger.debug("PROXY ECHO SERVER: listening to port: " + request.getPort());
            //set a timeout:
            server.setSoTimeout(request.getTimeout());

            ProxyEchoResult result = new ProxyEchoResult();

            try {
                Socket socket = server.accept();
                String msg = readMessage(socket, false);
                logger.debug("PROXY ECHO SERVER: received message: " + msg);
                sendMessage(socket, msg, true);

                result.setMessage(msg);
                result.setSuccess(true);
            } catch (SocketTimeoutException e) {
                logger.warn("PROXY ECHO SERVER: timeout reached. nothing received...");
                logger.error(e.getMessage());
                result.setSuccess(false);
            } catch (IOException e) {
                logger.warn("PROXY ECHO SERVER: could not send msg");
                logger.error(e.getMessage());
                result.setSuccess(false);
            }
            return result;
        }
    }

    /**
     * @param socket
     * @return
     * @throws IOException
     */
    public String readMessage(Socket socket, boolean closeSocket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        char[] buffer = new char[8192];
        int length = bufferedReader.read(buffer, 0, 8192); // waiting for Message
        String msg = new String(buffer, 0, length);

        if (closeSocket) {
            bufferedReader.close();
        }

        return msg;
    }

    /**
     * @param socket
     * @param message
     * @throws IOException
     */
    public void sendMessage(Socket socket, String message, boolean closeSocket) throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
        pw.println(message);

        if (closeSocket) {
            pw.close();
        }
    }
}
