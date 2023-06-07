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

import at.alladin.rmbt.qos.testserver.ServerPreferences.ServiceSetting;
import at.alladin.rmbt.qos.testserver.ServerPreferences.TestServerServiceEnum;
import at.alladin.rmbt.qos.testserver.ServerPreferences.UdpPort;
import at.alladin.rmbt.qos.testserver.entity.TestCandidate;
import at.alladin.rmbt.qos.testserver.service.EventJob.EventType;
import at.alladin.rmbt.qos.testserver.service.ServiceManager;
import at.alladin.rmbt.qos.testserver.tcp.TcpMultiClientServer;
import at.alladin.rmbt.qos.testserver.tcp.TcpWatcherRunnable;
import at.alladin.rmbt.qos.testserver.udp.*;
import at.alladin.rmbt.qos.testserver.util.RuntimeGuardService;
import at.alladin.rmbt.util.Randomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestServer {

    private static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    public final static String TEST_SERVER_VERSION_MAJOR = "3";
    public final static String TEST_SERVER_VERSION_MINOR = "1";
    public final static String TEST_SERVER_VERSION_PATCH = "0";
    public final static String TEST_SERVER_CODENAME = "DROPPED PACKET";
    public final static String TEST_SERVER_VERSION_NAME = "QoSTS \"" + TEST_SERVER_CODENAME + "\" " + TEST_SERVER_VERSION_MAJOR + "." + TEST_SERVER_VERSION_MINOR;
    public final static String QOS_KEY_FILE = "crt/qosserver.jks";
    //public final static String QOS_KEY_FILE = "qosserver.jks";
    public final static String QOS_KEY_TYPE = "JKS";
    public final static String QOS_KEY_PASSWORD = "123456qwertz";
    public final static String RMBT_ENCRYPTION_STRING = "TLS";
    public final static boolean USE_FIXED_THREAD_POOL = true;
    public final static int MAX_THREADS = 200;
    public final static TreeMap<Integer, List<AbstractUdpServer<?>>> udpServerMap = new TreeMap<Integer, List<AbstractUdpServer<?>>>();
    public final static ConcurrentHashMap<Integer, List<TcpMultiClientServer>> tcpServerMap = new ConcurrentHashMap<Integer, List<TcpMultiClientServer>>();
    public final static ServiceManager serviceManager = new ServiceManager();
    public final static Set<ClientHandler> clientHandlerSet = new HashSet<>();

    public final static Randomizer randomizer = new Randomizer(8000, 12000, 3);
    private static final ExecutorService COMMON_THREAD_POOL = Executors.newCachedThreadPool();
    public volatile static List<ServerSocket> serverSocketList = new ArrayList<ServerSocket>();
    public static SSLServerSocketFactory sslServerSocketFactory;
    public static ServerPreferences serverPreferences;

    private static ExecutorService mainServerPool;

    public static void main(String[] args) throws Exception {

        try {
            if (args.length > 0) {
                serverPreferences = new ServerPreferences(args);
            } else {
                serverPreferences = new ServerPreferences();
            }
        } catch (TestServerException e) {
            ServerPreferences.writeErrorString();
            e.printStackTrace();
            System.exit(0);
        }

        logger.info("Starting QoSTestServer (" + TEST_SERVER_VERSION_NAME);
        logger.info("Settings: " + serverPreferences.toString());

        //console.start();

        if (!USE_FIXED_THREAD_POOL) {
            mainServerPool = Executors.newCachedThreadPool();
        } else {
            mainServerPool = Executors.newFixedThreadPool(serverPreferences.getMaxThreads());
        }

        try {

            if (serverPreferences.useSsl()) {
                /*******************************
                 * initialize SSLContext and SSLServerSocketFactory:
                 */

                logger.info("Loading JKS ...");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

                // Load the JKS key chain
                KeyStore ks = KeyStore.getInstance(QOS_KEY_TYPE);
                InputStream fis = TestServer.class.getResourceAsStream(QOS_KEY_FILE);//new FileInputStream(serverKey);
                //InputStream fis = new FileInputStream(QOS_KEY_FILE);
                ks.load(fis, QOS_KEY_PASSWORD.toCharArray());
                fis.close();
                kmf.init(ks, QOS_KEY_PASSWORD.toCharArray());
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                // Initialize the SSL context
                sslContext.init(kmf.getKeyManagers(), new TrustManager[]{getTrustingManager()}, new SecureRandom());

                sslServerSocketFactory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();

                logger.info("Loading JKS ... DONE");
            }

            for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
                try {
                    ServerSocket serverSocket;
                    if (serverPreferences.useSsl()) {
                        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket();
                    } else {
                        serverSocket = new ServerSocket();
                    }

                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(addr, serverPreferences.getServerPort()));
                    serverSocketList.add(serverSocket);

                    Thread mainThread = new Thread(new QoSService(mainServerPool, serverSocket, null));
                    mainThread.start();
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }

            Iterator<UdpPort> portIterator = serverPreferences.getUdpPortSet().iterator();

            while (portIterator.hasNext()) {
                final List<AbstractUdpServer<?>> udpServerList = new ArrayList<AbstractUdpServer<?>>();
                final UdpPort udpPort = portIterator.next();
                for (InetAddress addr : serverPreferences.getInetAddrBindToSet()) {
                    AbstractUdpServer<?> udpServer = null;
                    try {
                        //udpServer = new UdpMultiClientServer(port, addr);
                        udpServer = udpPort.isNio ? new NioUdpMultiClientServer(udpPort.port, addr) : new UdpMultiClientServer(udpPort.port, addr);
                    } catch (Exception e) {
                        logger.error("TestServer INIT; Opening UDP Server on port: " + udpPort);
                        logger.error(e.getMessage());
                    }

                    if (udpServer != null) {
                        udpServerList.add(udpServer);
                        getCommonThreadPool().execute(udpServer);
                    }
                }

                if (udpServerList.size() > 0) {
                    udpServerMap.put(udpPort.port, udpServerList);
                }
            }

            //start UDP watcher service:
            final UdpWatcherRunnable udpWatcherRunnable = new UdpWatcherRunnable();
            serviceManager.addService(udpWatcherRunnable);
            //start TCP watcher service:
            final TcpWatcherRunnable tcpWatcherRunnable = new TcpWatcherRunnable();
            serviceManager.addService(tcpWatcherRunnable);
            //register runtime guard service:
            final RuntimeGuardService runtimeGuardService = new RuntimeGuardService();
            serviceManager.addService(runtimeGuardService);

            //dispatch onStart event:
            serviceManager.dispatchEvent(EventType.ON_TEST_SERVER_START);

            //finally, start all plugins & services:
            for (ServiceSetting service : serverPreferences.getPluginMap().values()) {
                logger.info("Starting plugin {}...", service.getName());
                service.start();
            }

            //add shutdown hook (ctrl+c):
            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        public void run() {
                            logger.info("CTRL-C. Close! ", -1, TestServerServiceEnum.TEST_SERVER);
                            shutdown();
                        }
                    });
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void shutdown() {
        logger.info("Shutting down QoS TestServer...", 0, TestServerServiceEnum.TEST_SERVER);


        //stop all plugins & services:
        for (ServiceSetting service : serverPreferences.getPluginMap().values()) {
            service.stop();
            logger.info("Plugin '" + service.getName() + "' stopped.", 0, TestServerServiceEnum.TEST_SERVER);
        }

        //dispatch onStop event:
        serviceManager.dispatchEvent(EventType.ON_TEST_SERVER_STOP);

        Map<String, Object> serviceResultMap = serviceManager.shutdownAll(true);
        if (serviceResultMap != null) {
            for (Entry<String, Object> entry : serviceResultMap.entrySet()) {
                if (entry.getValue() != null) {
                    logger.info("Service '" + entry.getKey() + "' result: " + entry.getValue());
                }
            }
        }

        mainServerPool.shutdownNow();
        try {
            mainServerPool.awaitTermination(4L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            for (ServerSocket serverSocket : serverSocketList) {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                        System.out.println("\nServerSocket: " + serverSocket + " closed.");
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
    }

    public static ServerSocket createServerSocket(int port, boolean isSsl, InetAddress inetAddress) throws IOException {
        ServerSocket socket = null;
        SocketAddress sa = new InetSocketAddress(inetAddress, port);
        if (!isSsl || !TestServer.serverPreferences.useSsl()) {
            socket = new ServerSocket();
        } else {
            socket = TestServer.sslServerSocketFactory.createServerSocket();
        }

        try {
            socket.bind(sa);
        } catch (Exception e) {
            logger.error("TCP " + port, "TCP Socket on port " + port);
            logger.error(e.getMessage());
            throw e;
        }

        socket.setReuseAddress(true);
        return socket;
    }

    public static DatagramSocket createDatagramSocket(int port, InetAddress addr) throws Exception {
        if (addr == null) {
            return new DatagramSocket(port);
        } else {
            return new DatagramSocket(port, addr);
        }
    }

    public static DatagramChannel createDatagramChannel(int port, InetAddress addr) throws Exception {
        final DatagramChannel channel = DatagramChannel.open();
        if (addr == null) {
            channel.bind(new InetSocketAddress(port));
        } else {
            channel.bind(new InetSocketAddress(addr, port));
        }

        return channel;
    }


    public static TrustManager getTrustingManager() {
        return new javax.net.ssl.X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException {
                System.out.println("[TRUSTING] checkClientTrusted: " +
                        Arrays.toString(certs) + " - " + authType);
            }

            public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException {
                System.out.println("[TRUSTING] checkServerTrusted: " +
                        Arrays.toString(certs) + " - " + authType);
            }
        };
    }

    public static SSLContext getSSLContext(final String keyResource, final String certResource)
            throws NoSuchAlgorithmException, KeyManagementException {
        X509Certificate _cert = null;
        try {
            if (certResource != null) {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                _cert = (X509Certificate) cf.generateCertificate(TestServer.class.getClassLoader().getResourceAsStream(
                        certResource));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final X509Certificate cert = _cert;

        TrustManagerFactory tmf = null;
        try {
            if (cert != null) {
                final KeyStore ks = KeyStore.getInstance("");
                ks.load(TestServer.class.getClassLoader().getResourceAsStream(keyResource), "199993ec".toCharArray());
                ks.setCertificateEntry("crt", cert);

                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        final TrustManager tm;
        if (cert == null)
            tm = getTrustingManager();
        else
            tm = new javax.net.ssl.X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{cert};
                }

                public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException {
                    System.out.println("checkClientTrusted: " +
                            Arrays.toString(certs) + " - " + authType);
                }

                public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException {
                    System.out.println("checkServerTrusted: " +
                            Arrays.toString(certs) + " - " + authType);
                    if (certs == null)
                        throw new CertificateException();
                    for (final X509Certificate c : certs)
                        if (cert.equals(c))
                            return;
                    throw new CertificateException();
                }
            };

        final TrustManager[] trustManagers = new TrustManager[]{tm};

        javax.net.ssl.SSLContext sc;
        sc = javax.net.ssl.SSLContext.getInstance(RMBT_ENCRYPTION_STRING);

        sc.init(null, trustManagers, new java.security.SecureRandom());
        return sc;
    }

    public static ConcurrentHashMap<Integer, List<TcpMultiClientServer>> getTcpServerMap() {
        return tcpServerMap;
    }

    public static synchronized void unregisterTcpServer(Integer port) {
        if (TestServer.tcpServerMap.remove(port) != null) {
            logger.info("Removed TCP server object on port: " + port);
        }
    }

    public static List<TcpMultiClientServer> registerTcpCandidate(Integer port, Socket socket) throws Exception {
        List<TcpMultiClientServer> tcpServerList;

        synchronized (TestServer.tcpServerMap) {
            //if port not mapped create complete tcp server list
            if ((tcpServerList = TestServer.tcpServerMap.get(port)) == null) {
                tcpServerList = new ArrayList<>();
                for (InetAddress addr : TestServer.serverPreferences.getInetAddrBindToSet()) {
                    tcpServerList.add(new TcpMultiClientServer(port, addr));
                }
                TestServer.tcpServerMap.put(port, tcpServerList);
            } else {
                //there are some tcp servers active on this port, compare ip list with tcp server list and create missing servers
                tcpServerList = TestServer.tcpServerMap.get(port);

                Set<InetAddress> inetAddrSet = new HashSet<>();
                for (TcpMultiClientServer tcpServer : tcpServerList) {
                    inetAddrSet.add(tcpServer.getInetAddr());
                }
                Set<InetAddress> inetAddrBindToSet = new HashSet<>(TestServer.serverPreferences.getInetAddrBindToSet());
                inetAddrBindToSet.removeAll(inetAddrSet);
                for (InetAddress addr : inetAddrBindToSet) {
                    tcpServerList.add(new TcpMultiClientServer(port, addr));
                }
            }

            //if ip check is enabled register the candidate's ip on the server's whitelist
            if (TestServer.serverPreferences.isIpCheck()) {
                for (TcpMultiClientServer tcpServer : tcpServerList) {
                    tcpServer.registerCandidate(socket.getInetAddress(), TestCandidate.DEFAULT_TTL);
                }
            }

            //prepare all servers on this port for incoming connections (open sockets/refresh ttl)
            for (TcpMultiClientServer tcpServer : tcpServerList) {
                tcpServer.prepare();
            }
        }

        return tcpServerList;
    }

    public static synchronized <T extends Closeable> AbstractUdpServer<T> registerUdpCandidate(final InetAddress localAddr, final int port, final String uuid, final UdpTestCandidate udpData) {
        try {
            logger.info("Trying to register UDP Candidate on " + localAddr + ":" + port);
            for (AbstractUdpServer<?> udpServer : TestServer.udpServerMap.get(port)) {
                logger.info("Comparing: " + localAddr + " <-> " + udpServer.getAddress());
                if (udpServer.getAddress().equals(localAddr) || udpServer.getAddress().isAnyLocalAddress()) {
                    logger.info("Registering UDP Candidate on " + localAddr + ":" + port);
                    logger.info("Registering UDP Candidate for UdpServer: " + udpServer.toString());
                    ((AbstractUdpServer<T>) udpServer).getIncomingMap().put(uuid, udpData);
                    return (AbstractUdpServer<T>) udpServer;
                }
            }
        } catch (Exception e) {
            logger.error("Register UDP candidate on port: " + port);
            logger.error(e.getMessage());
        }

        return null;
    }

    public static ExecutorService getCommonThreadPool() {
        return COMMON_THREAD_POOL;
    }
}
