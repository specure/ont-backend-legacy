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
package at.alladin.rmbt.qos.testserver.udp;

import at.alladin.rmbt.qos.testserver.entity.TestCandidate;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractUdpServer<T extends Closeable> implements Runnable {

    protected final ConcurrentHashMap<String, UdpTestCandidate> incomingMap = new ConcurrentHashMap<>();

    protected final Class<?> clazz;

    public AbstractUdpServer(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public synchronized UdpTestCandidate getClientData(String uuid) {
        return incomingMap.get(uuid);
    }

    public synchronized TestCandidate pollClientData(String uuid) {
        return incomingMap.remove(uuid);
    }

    public synchronized ConcurrentHashMap<String, UdpTestCandidate> getIncomingMap() {
        return incomingMap;
    }

    public abstract T getSocket();

    public abstract void send(DatagramPacket dp) throws IOException;

    public abstract boolean getIsRunning();

    public abstract InetAddress getAddress();

    public abstract int getLocalPort();
}
