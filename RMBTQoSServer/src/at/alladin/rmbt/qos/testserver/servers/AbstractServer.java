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
package at.alladin.rmbt.qos.testserver.servers;

import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractServer<T extends AutoCloseable, H extends TestCandidate> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);

    protected final Class<T> serverSocketTypeClazz;
    protected final Class<H> clientDataHolderClazz;

    protected final ConcurrentHashMap<InetAddress, H> candidateMap = new ConcurrentHashMap<>();
    protected final AtomicLong ttlTimestamp = new AtomicLong(0);
    protected final int port;
    protected final InetAddress inetAddr;
    private final String name;
    private final TestServerServiceEnum testServerService;
    protected T serverSocket;

    public AbstractServer(Class<T> serverSocketTypeClazz, Class<H> clientHandlerRunnableClazz,
                          InetAddress inetAddr, int port, String tag, TestServerServiceEnum testServerService) {
        this.serverSocketTypeClazz = serverSocketTypeClazz;
        this.clientDataHolderClazz = clientHandlerRunnableClazz;
        this.name = tag + " [" + inetAddr + ":" + port + "]";
        this.port = port;
        this.inetAddr = inetAddr;
        this.testServerService = testServerService;
    }

    public abstract void prepare() throws Exception;

    public abstract boolean close() throws Exception;

    public abstract boolean isAlive();

    protected abstract void execute() throws Exception;

    public T getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(T serverSocket) {
        this.serverSocket = serverSocket;
    }

    public String getName() {
        return name;
    }

    public AtomicLong getTtlTimestamp() {
        return ttlTimestamp;
    }

    public void refreshTtl(long byValue) {
        ttlTimestamp.set(System.currentTimeMillis() + byValue);
        logger.info(getName() + " Refreshing TTL to: " + ttlTimestamp.get());
    }

    public int getPort() {
        return port;
    }

    public InetAddress getInetAddr() {
        return inetAddr;
    }

    protected synchronized void closeSocket() {
        try {
            this.serverSocket.close();
            this.serverSocket = null;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        logger.info(getName() + " started!");

        try {
            while (isAlive()) {
                execute();
            }

        } catch (SocketTimeoutException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.warn(e.getMessage());
        } finally {
            try {
                //close this socket if necessary
                close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        logger.info(getName() + " closed!");
    }

    public synchronized TestCandidate registerCandidate(InetAddress candidateInetAddress, int resetTtl) {
        try {
            TestCandidate candidate = null;
            if (candidateMap.containsKey(candidateInetAddress)) {
                candidate = (TestCandidate) candidateMap.get(candidateInetAddress);
            } else {
                candidate = clientDataHolderClazz.newInstance();
                candidate.setTtl(resetTtl);
            }

            candidate.increaseTestCounter(true);

            logger.info(getName() + " Registering candidate " + candidateInetAddress + ": " + candidate + ")");

            candidateMap.put(candidateInetAddress, (H) candidate);

            return candidate;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public synchronized void removeCandidate(InetAddress candidateInetAddress) {
        if (candidateMap.containsKey(candidateInetAddress)) {
            TestCandidate candidate = candidateMap.get(candidateInetAddress);
            if (candidate.decreaseTestCounter(true) <= 0) {
                logger.info(getName() + " Candidate " + candidateInetAddress + " has no more tests left.");
                candidateMap.remove(candidateInetAddress);
            } else {
                logger.info(getName() + " Candidate (" + candidate + " - " + candidateInetAddress + ") has " + (candidate.getTestCounter()) + " tests left.");
            }
        }
    }

    public Map<InetAddress, H> getCandidateMap() {
        return candidateMap;
    }
}
