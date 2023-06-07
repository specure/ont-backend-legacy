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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lb
 */
public class ProxyEchoService extends Thread {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(ProxyEchoService.class);

    //eager initialization -> makes synchronization of getInstance() unnecessary
    private static ProxyEchoService instance = new ProxyEchoService();

    private final static int MAX_THREADS = 20;

    private final ExecutorService serverSocketPool;

    private final Queue<ProxyEchoRequest> requests;

    private volatile boolean run = true;

    /**
     * @return
     */
    public static ProxyEchoService getInstance() {
        return instance;
    }

    /**
     *
     */
    private ProxyEchoService() {
        super();
        serverSocketPool = Executors.newFixedThreadPool(MAX_THREADS);
        requests = new ConcurrentLinkedQueue<>();
        start();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        logger.debug("PROXY ECHO SERVER: starting");
        while (run) {
            if (!requests.isEmpty()) {
                serverSocketPool.submit(new ProxyEchoServerSocket(requests.poll()));
            }
        }
        serverSocketPool.shutdownNow();
        logger.debug("PROXY ECHO SERVER: stopping");
    }

    /**
     *
     */
    public void terminate() {
        run = false;
    }

    /**
     * @param request
     * @return
     */
    public synchronized void listen(final ProxyEchoRequest request) {
        logger.debug("PROXY ECHO SERVER: adding new request to queue on port: " + request.getPort());
        requests.add(request);
    }


}
