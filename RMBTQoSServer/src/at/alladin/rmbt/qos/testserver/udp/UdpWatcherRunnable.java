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
import at.alladin.rmbt.qos.testserver.service.IntervalJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class UdpWatcherRunnable extends IntervalJob<String> {

    private static final Logger logger = LoggerFactory.getLogger(UdpWatcherRunnable.class);
    public final static String TAG = UdpWatcherRunnable.class.getCanonicalName();
    public final static boolean RESTART_ON_ERROR = true;
    protected long removeCounter = 0;

    public UdpWatcherRunnable() {
        super(TestServerServiceEnum.UDP_SERVICE);
    }

    @Override
    public String execute() {
        if (TestServer.udpServerMap != null) {
            synchronized (TestServer.udpServerMap) {
                Iterator<List<AbstractUdpServer<?>>> listIterator = TestServer.udpServerMap.values().iterator();
                while (listIterator.hasNext()) {
                    Iterator<AbstractUdpServer<?>> iterator = listIterator.next().iterator();
                    while (iterator.hasNext()) {
                        AbstractUdpServer<?> udpServer = iterator.next();
                        Iterator<?> incomingMapIterator = udpServer.getIncomingMap().entrySet().iterator();
                        while (incomingMapIterator.hasNext()) {
                            @SuppressWarnings("unchecked")
                            Entry<String, UdpTestCandidate> entry = (Entry<String, UdpTestCandidate>) incomingMapIterator.next();
                            if (entry.getValue().getTtl() < System.currentTimeMillis()) {
                                logger.info("UDP Client (ServerPort: " + udpServer.getLocalPort() + ") TTL reached and removed: " + entry.getValue());
                                incomingMapIterator.remove();
                                removeCounter++;
                            }
                        }
                    }
                }
            }
        }
        return "removed dead candidates: " + removeCounter;
    }

    @Override
    public boolean restartOnError() {
        return RESTART_ON_ERROR;
    }

    @Override
    public UdpWatcherRunnable getNewInstance() {
        return new UdpWatcherRunnable();
    }

    @Override
    public String getId() {
        return TAG;
    }
}
