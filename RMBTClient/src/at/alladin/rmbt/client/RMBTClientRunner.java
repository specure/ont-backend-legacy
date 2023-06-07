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
package at.alladin.rmbt.client;

import at.alladin.rmbt.client.helper.*;
import at.alladin.rmbt.client.ndt.NDTRunner;
import at.alladin.rmbt.client.v2.task.QoSTestEnum;
import at.alladin.rmbt.client.v2.task.result.QoSResultCollector;
import at.alladin.rmbt.client.v2.task.service.TestSettings;
import com.google.gson.*;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.measurementlab.ndt.NdtTests;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RMBTClientRunner {

    private static final String APPLICATION_NAME = "RMBT Command Line Client";
    private static final String MODEL_NAME = "JAVA CLI";
    private static final String CLIENT_UUID_KEY = "NettestClientUuid_";
    private static final String DEFAULT_RESULT_URL = "https://%s/en/history/";
    private static final String DEFAULT_HOST = "nettest.org";
    private static final int DEFAULT_PORT = 443;

    public static final String RMBT_CUSTOMER_HEADER = "x-nettest-client";
    public static final String RMBT_CUSTOMER_VALUE_DEFAULT = "nettest";

    /**
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void main(final String[] args) throws IOException, InterruptedException, KeyManagementException,
            NoSuchAlgorithmException {
        final OptionParser parser = new OptionParser() {
            {
                acceptsAll(Arrays.asList("?", "help"), "Show help");

                acceptsAll(Arrays.asList("h", "host"), "RMBT server IP or hostname (required)").withRequiredArg()
                        .ofType(String.class);

                acceptsAll(Arrays.asList("p", "port"), "RMBT server port (required)").withRequiredArg().ofType(
                        Integer.class);

                acceptsAll(Arrays.asList("s", "ssl"), "Use SSL/TLS");

//                acceptsAll(Arrays.asList("ssl-no-verify"), "Turn off SSL/TLS certificate validation");
//
//                acceptsAll(Arrays.asList("t", "threads"), "Number of threads (required when dev-mode)")
//                        .withRequiredArg().ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("d", "duration"), "Test duration in seconds (required when dev-mode)")
//                        .withRequiredArg().ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("n", "ndt"), "Run NDT after RMBT");

                acceptsAll(Arrays.asList("v", "verbose"), "Show verbose output");

                acceptsAll(Arrays.asList("q", "qos"), "Run QOS after RMBT");

//                acceptsAll(Arrays.asList("ndt-host"), "NDT host to use").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("u", "uuid"), "User's uuid").withRequiredArg()
//                        .ofType(String.class);

                acceptsAll(Arrays.asList("o", "open"), "Open test result in browser");

//                acceptsAll(Arrays.asList("l", "loop"), "Enable loop mode with defined count of measurements").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("i", "interval"), "Interval in seconds between two measurements in loop mode")
//                        .withRequiredArg().ofType(Integer.class);

                acceptsAll(Arrays.asList("g", "gui"), "Show the test progress in Graphical User Interface");

//                acceptsAll(Arrays.asList("g-dev", "gui-dev-mode"), "Show the test progress in Developer's Graphical User Interface");
//
                acceptsAll(Arrays.asList("tag"), "Tag the test with custom word").withRequiredArg()
                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("device"), "Tag device with custom word").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("model"), "Tag model with custom word").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("network_type"), "Tag network_type with custom word").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("network_country"), "Tag network_country with custom word").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("network_operator"), "Tag network_operator with custom word").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("network_operator_name"), "Tag network_operator_name with custom word").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("lte_rsrp"), "Tag lte_rsrp with custom word").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("lte_rsrq"), "Tag lte_rsrq with custom word").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("rssi"), "Tag rssi with custom word").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("cell_info"), "Tag cell_info with custom word").withRequiredArg()
//                        .ofType(Integer.class);
//
//                acceptsAll(Arrays.asList("geo_lat"), "Tag geo_lat with custom word").withRequiredArg()
//                        .ofType(Double.class);
//
//                acceptsAll(Arrays.asList("geo_long"), "Tag geo_long with custom word").withRequiredArg()
//                        .ofType(Double.class);
//
//                acceptsAll(Arrays.asList("geo_accuracy"), "Tag geo_accuracy with custom word").withRequiredArg()
//                        .ofType(Double.class);
//
//                acceptsAll(Arrays.asList("on_net"), "Tag on_net").withRequiredArg()
//                        .ofType(Boolean.class);
//
//                acceptsAll(Arrays.asList("ifport"), "Tag ifport").withRequiredArg()
//                        .ofType(String.class);
//
//                acceptsAll(Arrays.asList("legacy"), "Legacy(old) Control Server (default = false)").withRequiredArg()
//                        .ofType(Boolean.class);
//
//                acceptsAll(Arrays.asList("c","customer"), "Customer (default = sah)").withRequiredArg()
//                        .ofType(String.class);

                //-h nettest.org -p 443 -v -o -s -tag ponto -device pontodevice -model pontodevice -network_country sk -network_type 13 -network_operator 231-06 -lte_rsrp -105 -lte_rsrq -10 -rssi 50 -cell_info 3014 -geo_lat 49.19242323 -geo_long 18.733000887 -geo_accuracy 25.5
            }
        };

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (final OptionException e) {
            System.out.println(String.format("error while parsing command line options: %s", e.getLocalizedMessage()));
            System.exit(1);
            return;
        }

        System.out.println(String.format("=============== RMBTClient %s ===============",
                RevisionHelper.getVerboseRevision()));

        // @MP, 2017-07-26, option -u (client UUID) is not mandatory anymore
        //final String[] requiredArgs = { "h", "p", "u" };
        // @MP, 2017-09-19, no options are mandatory, we are going to way of default values
        final String[] requiredArgs = {};

        // test of SSL options
        if (options.has("ssl-no-verify"))
            SSLContext.setDefault(RMBTClient.getSSLContext(null, null));
        else
            SSLContext.setDefault(RMBTClient.getSSLContext("at/alladin/rmbt/crt/ca.pem",
                    "at/alladin/rmbt/crt/controlserver.pem"));

        boolean reqArgMissing = false;
        if (!options.has("?"))
            for (final String arg : requiredArgs)
                if (!options.has(arg)) {
                    reqArgMissing = true;
                    System.out.println(String.format("ERROR: required argument '%s' is missing", arg));
                }
        if (options.has("?") || reqArgMissing) {
            System.out.println();
            parser.printHelpOn(System.out);
            System.exit(1);
            return;
        }

        // check hostname
        final String host = (options.has("h") && ((String) options.valueOf("h")).isEmpty() == false) ? (String) options.valueOf("h") : DEFAULT_HOST;

        // check port
        final Integer port = (options.has("p") && options.valueOf("p") instanceof Integer) ? (Integer) options.valueOf("p") : DEFAULT_PORT;

        // check encryption
        final boolean encryption = (port == 443 || options.has("s")) ? true : false;

        // verbose outputs
        final boolean verbose = options.has("v");

        // check if result should be opened in browser
        final boolean openResultsInBrowser = options.has("o");

//        // check if loop mode has to be enabled
//        final boolean isLoopMoodEnabled = options.has("l");
//
//        // default loops count
//        int countOfMeasurements = 100;
//
//        // default interval 60 seconds
//        int interval = 60000;//mms
//        if (isLoopMoodEnabled) {
//            countOfMeasurements = (int) options.valueOf("l");
//
//            // check interval
//            if (options.has("i") && options.valueOf("i") != null && ((int) options.valueOf("i") > 0)) {
//                interval = ((int) options.valueOf("i")) * 1000;
//            }
//
//            System.out.println("Loop mode is enabled. Loops count is " + countOfMeasurements + " and interval between two measurements is " + (interval / 1000) + " seconds...");
//        }

        // start QoS tests also
        final boolean runQoS = options.has("q");

//        // start Ndt tests also
//        final boolean runNdt = options.has("n");
//
//        // tag the test
        final String tag = options.has("tag") ? (String) options.valueOf("tag") : null;
//        final String tag_device = options.has("device") ? (String) options.valueOf("device") : null;
//        final String tag_model = options.has("model") ? (String) options.valueOf("model") : null;
//        final Integer tag_network_type = options.has("network_type") ? (Integer) options.valueOf("network_type") : null;
//        final String tag_network_country = options.has("network_country") ? (String) options.valueOf("network_country") : null;
//        final String tag_network_operator = options.has("network_operator") ? (String) options.valueOf("network_operator") : null;
//        final String tag_network_operator_name = options.has("network_operator_name") ? (String) options.valueOf("network_operator_name") : null;
//        final Integer tag_lte_rsrp = options.has("lte_rsrp") ? (Integer) options.valueOf("lte_rsrp") : null;
//        final Integer tag_lte_rsrq = options.has("lte_rsrq") ? (Integer) options.valueOf("lte_rsrq") : null;
//        final Integer tag_rssi = options.has("rssi") ? (Integer) options.valueOf("rssi") : null;
//        final Integer tag_cell_info = options.has("cell_info") ? (Integer) options.valueOf("cell_info") : null;
//        final Double geo_lat = options.has("geo_lat") ? (Double) options.valueOf("geo_lat") : null;
//        final Double geo_long = options.has("geo_long") ? (Double) options.valueOf("geo_long") : null;
//        final Double geo_accuracy = options.has("geo_accuracy") ? (Double) options.valueOf("geo_accuracy") : null;
//        final Boolean on_net = options.has("on_net") ? (Boolean) options.valueOf("on_net") : null;
//        final String ifport = options.has("ifport") ? (String) options.valueOf("ifport") : null;
//        final Boolean legacy_server = options.has("legacy") ? (Boolean) options.valueOf("legacy") : true;
//        final String customer = options.has("c") ? (String) options.valueOf("c") : options.has("customer") ? (String) options.valueOf("customer") : RMBT_CUSTOMER_VALUE_DEFAULT;

        // check if GUI has to be visible
        if (options.has("g")) {
            // show GUI to see test outputs

            RMBTClientJFrame frame = new RMBTClientJFrame(APPLICATION_NAME, RMBTClientJFrame.ClientMode.NORMAL);

            // redirect outputs to gui
            System.setOut(new PrintStream(frame.getOutputStream()));
            System.setErr(new PrintStream(frame.getOutputStream()));

            // show GUI
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.setVisible(true);
                }
            });

            // perform tests
            performTest(options, host, port, encryption, verbose, runQoS, false, openResultsInBrowser, false, 0, 0,
                    tag, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true, null);

            // exit
            System.exit(0);

        } else {
            // perform tests
            performTest(options, host, port, encryption, verbose, runQoS, false, openResultsInBrowser, false, 0, 0,
                    tag, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, true, null);

            // exit
            System.exit(0);
        }

    }

    private static void performTest(OptionSet options, String host, Integer port, boolean encryption, boolean verbose, boolean runQoS, boolean runNdt, boolean openResultsInBrowser, boolean isLoopMoodEnabled, int countOfMeasurements, int interval,
                                    String tag, String device,  String tag_model, Integer network_type, String network_country, String network_operator, String network_operator_name,  Integer lte_rsrp, Integer lte_rsrq, Integer cell_info,
                                    Integer rssi, Double geo_lat, Double geo_long, Double geo_accuracy, Boolean on_net, String ifport, Boolean legacyServer, String customer) throws InterruptedException {

        final ArrayList<String> geoInfo = null;

        int numThreads = 0;
        int duration = 0;
        if (options.has("t"))
            numThreads = (Integer) options.valueOf("t");
        if (options.has("d"))
            duration = (Integer) options.valueOf("d");

        int numPings = 10;

        RMBTTestParameter overrideParams = null;
        if (numThreads > 0 || duration > 0)
            overrideParams = new RMBTTestParameter(null, 0, false, duration, numThreads, numPings);

        // check client UUID
        // 1. check if UUID has been entered as a startup parameter, if yes, store it
        // 2. if not, check if UUID is stored locally
        // 3. if not, try to get UUID from server

        Preferences prefs = Preferences.userRoot();

        // 1. check if UUID has been entered as a startup parameter
        String uuid = (String) options.valueOf("u"); //"47bc7f65-47d7-457d-9a69-56b2b71ebd9f";
        if (uuid == null || uuid.isEmpty()) {
            // UUID not entered, try to get it from client or server
            System.out.println("Client UUID NOT entered as a startup parameter! Try to find it on client side...");

            uuid = prefs.get(CLIENT_UUID_KEY + host, null);

            // 2. check if UUID found on client side
            if (uuid == null || uuid.isEmpty()) {
                // 3. UUID not found on client side, try to get it from server
                System.out.println("Client UUID NOT found on client side! Try to get it from server side...");

                // get UUID from server
                uuid = RMBTClient.requestNewClientUUID(customer, legacyServer, host, null, port, encryption, geoInfo, "",
                        "DESKTOP", Config.RMBT_CLIENT_NAME, Config.RMBT_VERSION_NUMBER, overrideParams, null);

                // test if UUID is valid
                try {
                    UUID testOfUUID = UUID.fromString(uuid);
                } catch (IllegalArgumentException | NullPointerException exception) {
                    System.out.println("Server returned invalid UUID!!! Program will terminate!");
                    System.exit(1);
                }

                // check UUID returned from server
                if (uuid != null && uuid.isEmpty() == false) {
                    // client UUID generated on server side
                    System.out.println("Client UUID generated on server side. Client UUID: " + uuid);

                    // store it for further executions
                    storeUUID(prefs, host, uuid);

                } else {
                    // client UUID NOT generated on server side
                    System.out.println("Client UUID NOT generated on server side!!! Program will terminate!!!");
                    System.exit(1);
                }//if - UUID generated by server

            } else {
                // client UUID found on client side
                System.out.println("Client UUID found on client side. Client UUID: " + uuid);

            }// if - UUID found on client

        } else {
            System.out.println("Client UUID entered as a startup parameter. Client UUID: " + uuid);

            // store it for further executions
            storeUUID(prefs, host, uuid);

        }// if - UUID as a startup parameter

        // start test
        System.out.println("STARTING TEST");
        //
        int i = 0;
        String loopUuid = null;
        do {
            try {
                // print loop in loop mode
                if (isLoopMoodEnabled) System.out.println("Loop: " + ++i);

                final JSONObject additionalValues = new JSONObject();
                if(on_net != null)
                    additionalValues.put("on-net", on_net);
                if(ifport != null)
                    additionalValues.put("ifport", ifport);

                // get client instance
                final RMBTClient client = RMBTClient.getInstance(customer, legacyServer, host, null, port, encryption, geoInfo, uuid,
                        "DESKTOP", Config.RMBT_CLIENT_NAME, Config.RMBT_VERSION_NUMBER, overrideParams, additionalValues);

                if (client == null) {
                    System.err.println("Unable to create client. Finishing");
                } else {

                    client.setOutputToStdout(verbose);

                    // run test
                    final TestResult result = client.runTest();

                    if (result != null) {
                        final JsonObject jsonResult = new JsonObject();
                        try {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            // plattform
                            jsonResult.add("plattform", gson.toJsonTree("CLI"));

                            // tag
                            jsonResult.add("tag", gson.toJsonTree(tag));

                            // model
                            if(tag_model == null || tag_model.isEmpty()) {
                                jsonResult.add("model", gson.toJsonTree(MODEL_NAME));
                            } else {
                                jsonResult.add("model", gson.toJsonTree(tag_model));
                            }

                            // device
                            if(device != null && !device.isEmpty()) {
                                jsonResult.add("device", gson.toJsonTree(device));
                            }

                            // network_type
                            if (network_type == null) {
                                jsonResult.add("network_type", gson.toJsonTree("97"));
                            } else {
                                jsonResult.add("network_type", gson.toJsonTree(network_type));

                            }

                            // network_country
                            if(network_country != null && !network_country.isEmpty()) {
                                jsonResult.add("telephony_network_country", gson.toJsonTree(network_country));
                                jsonResult.add("telephony_network_sim_country", gson.toJsonTree(network_country));
                            }

                            // network_operator
                            if(network_operator != null && !network_operator.isEmpty()) {
                                jsonResult.add("telephony_network_operator", gson.toJsonTree(network_operator));
                                jsonResult.add("telephony_network_sim_operator", gson.toJsonTree(network_operator));
                            }

                            // telephony_network_operator_name
                            if(network_operator_name != null && !network_operator_name.isEmpty()) {
                                jsonResult.add("telephony_network_operator_name", gson.toJsonTree(network_operator_name));
                                //jsonResult.add("telephony_network_sim_operator_name", gson.toJsonTree(network_operator_name));
                            }
//                            // network_group_name
//                            if(network_group_name != null && !network_group_name.isEmpty()) {
//                                jsonResult.add("network_group_name", gson.toJsonTree(network_group_name));
//                            }


                            // cells_info
                            if(cell_info != null) {
                                JsonArray jsonArray = new JsonArray();

                                JsonObject jsonArrayItem = new JsonObject();
                                jsonArrayItem.addProperty("location_id", cell_info);
                                jsonArray.add(jsonArrayItem);

                                jsonResult.add("cellLocations", jsonArray);
                            }

                            // lte_rsrp, lte_rsrq, signal_strength
                            JsonArray jsonArray = new JsonArray();
                            JsonObject jsonArrayItem = new JsonObject();
                            if (lte_rsrp != null) jsonArrayItem.addProperty("lte_rsrp", lte_rsrp);
                            if (lte_rsrq != null) jsonArrayItem.addProperty("lte_rsrq", lte_rsrq);
                            if (rssi != null) jsonArrayItem.addProperty("signal_strength", rssi);
                            jsonArray.add(jsonArrayItem);
                            jsonResult.add("signals", jsonArray);

                            // geo location
                            if(geo_lat != null && geo_long != null && geo_accuracy != null) {
                                JsonArray geoJsonArray = new JsonArray();
                                JsonObject geoJsonArrayItem = new JsonObject();
                                geoJsonArrayItem.addProperty("geo_lat", geo_lat);
                                geoJsonArrayItem.addProperty("geo_long", geo_long);
                                geoJsonArrayItem.addProperty("accuracy", geo_accuracy);
                                geoJsonArrayItem.addProperty("provider", "FUSED");
                                geoJsonArrayItem.addProperty("tstamp", System.currentTimeMillis());

                                geoJsonArray.add(geoJsonArrayItem);
                                jsonResult.add("geoLocations", geoJsonArray);
                            }

                            // check if loop mode is enabled, if yes, then add attribute "loop_uuid" to test result
                            if (isLoopMoodEnabled && ((loopUuid != null && loopUuid.isEmpty() == false) || (client.getTestUuid() != null && client.getTestUuid().isEmpty() == false))) {
                                if (loopUuid == null || loopUuid.isEmpty()) loopUuid = client.getTestUuid();
                                jsonResult.add("loop_uuid", gson.toJsonTree(loopUuid));
                            }
                        } catch (JsonParseException e) {
                            //e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                        client.sendResult(customer, jsonResult);
                    }

                    client.shutdown();

                    if (runQoS) {
                        try {
                            client.log("Starting QoS Test... ");
                            TestSettings nnTestSettings = new TestSettings(client.getControlConnection().getStartTimeNs());
                            // always turn on SSL for QoS tests
                            nnTestSettings.setUseSsl(true);
                            QualityOfServiceTest nnTest = new QualityOfServiceTest(client, nnTestSettings);
                            QoSResultCollector nnResult = nnTest.call();

                            System.out.println("QoS finished.");
                            if (nnResult != null && nnTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
                                client.log("Sending QoS results... ");
                                client.sendQoSResult(customer, nnResult);
                                client.log("Sending QoS test finished");
                            } else {
                                System.err.println("Error during QoS test.");
                            }

                            //client.shutdown();

                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.err.println(e.getMessage());
                        }
                    } else {
                        client.log("Skipping QoS test....");
                        client.setStatus(TestStatus.END);
                    }

                    //if (client.getStatus() != TestStatus.END)
                    if (client.getStatus() != TestStatus.END && client.getStatus() != TestStatus.SPEEDTEST_END && client.getStatus() != TestStatus.QOS_END)
                        System.err.println("ERROR while checking client status. Expecting 'END' or 'SPEEDTEST_END' or 'QOS_END', got : '" + client.getStatus().toString() + "'. Message:" + client.getErrorMsg());
                    else {
                        if (runNdt) {
                            //System.out.println("\n\nStarting NDT...");

                            String ndtHost = null;
                            if (options.has("ndt-host"))
                                ndtHost = (String) options.valueOf("ndt-host");

                            final NDTRunner ndtRunner = new NDTRunner(ndtHost);
                            ndtRunner.runNDT(NdtTests.NETWORK_WIRED, ndtRunner.new UiServices() {
                                @Override
                                public void appendString(String str, int viewId) {
                                    super.appendString(str, viewId);
                                    //                            if (viewId == MAIN_VIEW)
                                    //System.out.println(str);
                                }

                                @Override
                                public void sendResults() {
                                    //System.out.println("sending NDT results...");
                                    client.getControlConnection().sendNDTResult(customer, this, null);
                                }
                            });

                            //client.shutdown();
                        }
                    }

                    // open browser
                    // get test params
                    //final RMBTTestParameter params = client.getControlConnection().getTestParameter(overrideParams);

                    // compose url for tests result
                    ControlServerConnection controlServerConnection = client.getControlConnection();
                    String resultUrl;
                    if (controlServerConnection != null && controlServerConnection.getResultURL() != null
                            && controlServerConnection.getResultURL().isEmpty() == false && client.getTestUuid() != null && client.getTestUuid().isEmpty() == false) {
                        resultUrl = (client.getControlConnection().getResultURL().contains(host)) ? client.getControlConnection().getResultURL() : String.format(DEFAULT_RESULT_URL, host);
                    } else {
                        resultUrl = String.format(DEFAULT_RESULT_URL, DEFAULT_HOST);
                    }
                    resultUrl += client.getTestUuid();
//                    resultUrl = String.format(DEFAULT_RESULT_URL, host) + client.getTestUuid();


                    // check, if has to be opened browser with test results
                    if (openResultsInBrowser) {
                        // open browser
                        System.out.println("Trying to open following URL with test result in default browser: " + resultUrl);
                        BrowserShow.showResults(resultUrl);
                    } else {
                        // print URL where to see results
                        System.out.println("To see test result open browser with url: " + resultUrl);
                    }
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            // sleep if enabled loop mode
            if (isLoopMoodEnabled && i < countOfMeasurements) {
                try {
                    System.out.println("Sleeping for " + (interval / 1000) + " seconds...");
                    // sleep for one minute
                    Thread.sleep(interval);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }

        } while (isLoopMoodEnabled && (i < countOfMeasurements));// end of cycle(loop mode)

        System.out.println("ENDING TEST");
    }


    private static void storeUUID(Preferences preferences, String host, String uuid) {
        preferences.put(CLIENT_UUID_KEY + host, uuid);
        try {
            preferences.flush();
            System.out.println("Client UUID has been stored on client side for further usage...");
        } catch (BackingStoreException e) {
            System.err.println("An error occurred while storing client UUID!!!");
        }// try - flush

    }
}