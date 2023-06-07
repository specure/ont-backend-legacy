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
package at.alladin.rmbt.qos.testserver;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class QoSService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(QoSService.class);

    private final static String TAG = QoSService.class.getCanonicalName();

    protected final ExecutorService executor;

    protected final ServerSocket socket;

    protected final SSLContext sslContext;

    private final String name;

    public QoSService(ExecutorService executor, ServerSocket socket, SSLContext sslContext) {
        this.executor = executor;
        this.socket = socket;
        this.sslContext = sslContext;
        this.name = "[QoSService " + socket.getInetAddress() + ":" + socket.getLocalPort() + "]: ";
    }

    @Override
    public void run() {
        logger.info("QoSService started on: " + socket + ". Awaiting connections...");
        try {
            while (true) {
                try {
                    if (Thread.interrupted() || socket.isClosed()) {
                        throw new InterruptedException();
                    }

                    Socket client = socket.accept();
                    executor.execute(new ClientHandler(socket, client));
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        throw e;
                    }
                    logger.error(e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            logger.error(name + "Interrupted! Shutting down!");
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(name + "Exception. Shutting down.");
            logger.error(e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
