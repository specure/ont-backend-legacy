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
package at.alladin.rmbt.controlServer;

import at.alladin.rmbt.controlServer.OffOnNet.controller.*;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;


public class ControlServer extends Application {

    /**
     * Public Constructor to create an instance of DemoApplication.
     *
     * @param parentContext - the org.restlet.Context instance
     */
    public ControlServer(final Context parentContext) {
        super(parentContext);
    }

    /**
     * The Restlet instance that will call the correct resource depending up on
     * URL mapped to it.
     *
     * @return -- The resource Restlet mapped to the URL.
     */
    @Override
    public Restlet createInboundRoot() {

        final Router router = new Router(getContext());

        router.attach("/version", VersionResource.class);
        router.attach("/V2/version", VersionResource.class);
        router.attach("/V3/version", VersionResource.class);

        // return info about selected measurement server
        router.attach("/measurementServer", MeasurementResource.class);
        router.attach("/V2/measurementServer", MeasurementResourceV2.class);
        router.attach("/V3/measurementServer", MeasurementResourceV2.class);

        // test request
        router.attach("/", RegistrationResource.class); // old URL, for backwards compatibility
        router.attach("/testRequest", RegistrationResource.class);
        router.attach("/V2/", RegistrationResource.class); // old URL, for backwards compatibility
        router.attach("/V2/testRequest", RegistrationResource.class);
        router.attach("/V3/", RegistrationResource.class); // old URL, for backwards compatibility
        router.attach("/V3/testRequest", RegistrationResource.class);
        router.attach("/V4/", OffOnNetRegistrationResource.class); // off/on net servers, for backwards compatibility

        // test result is submitted, will be called once only
        router.attach("/result", ResultResource.class);
        router.attach("/V2/result", ResultResource.class);
        router.attach("/V3/result", ResultResource.class);

        router.attach("/resultQoS", QualityOfServiceResultResource.class);
        router.attach("/V2/resultQoS", QualityOfServiceResultResource.class);
        router.attach("/V3/resultQoS", QualityOfServiceResultResource.class);

        // plz is submitted (optional additional resource for browser)
        router.attach("/resultUpdate", ResultUpdateResource.class);
        router.attach("/V2/resultUpdate", ResultUpdateResource.class);
        router.attach("/V3/resultUpdate", ResultUpdateResource.class);

        // ndt test results are submitted (optional, after /result)
        router.attach("/ndtResult", NdtResultResource.class);
        router.attach("/V2/ndtResult", NdtResultResource.class);
        router.attach("/V3/ndtResult", NdtResultResource.class);

        router.attach("/news", NewsResource.class);
        router.attach("/V2/news", NewsResource.class);
        router.attach("/V3/news", NewsResource.class);

        router.attach("/ip", IpResource.class);
        router.attach("/V2/ip", IpResource.class);
        router.attach("/V3/ip", IpResource.class);

        router.attach("/status", StatusResource.class);
        router.attach("/V2/status", StatusResource.class);
        router.attach("/V3/status", StatusResource.class);

        // send history list to client
        router.attach("/history", HistoryResource.class);
        router.attach("/V2/history", HistoryResource.class);
        router.attach("/V3/history", HistoryResource.class);

        // send brief summary of test results to client
        router.attach("/testresult", TestResultResource.class);
        router.attach("/V2/testresult", TestResultResource.class);
        router.attach("/V3/testresult", TestResultResource.class);

        // send detailed test results to client
        router.attach("/testresultdetail", TestResultDetailResource.class);
        router.attach("/V2/testresultdetail", TestResultDetailResource.class);
        router.attach("/V3/testresultdetail", TestResultDetailResource.class);

        router.attach("/sync", SyncResource.class);
        router.attach("/V2/sync", SyncResource.class);
        router.attach("/V3/sync", SyncResource.class);

        router.attach("/settings", SettingsResource.class);
        router.attach("/V2/settings", SettingsResource.class);
        router.attach("/V3/settings", SettingsResource.class);

        // collection of UserAgent etc.for IE (via server)
        router.attach("/requestDataCollector", RequestDataCollector.class);
        router.attach("/V2/requestDataCollector", RequestDataCollector.class);
        router.attach("/V3/requestDataCollector", RequestDataCollector.class);

        router.attach("/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/opentests/O{open_test_uuid}", OpenTestResource.class);
        router.attach("/V2/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/V2/opentests/O{open_test_uuid}", OpenTestResource.class);
        router.attach("/V3/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/V3/opentests/O{open_test_uuid}", OpenTestResource.class);

        router.attach("/qos/O{open_test_uuid}", OpenTestQoSResource.class);
        router.attach("/qos/O{open_test_uuid}/{lang}", OpenTestQoSResource.class);
        router.attach("/V2/qos/O{open_test_uuid}", OpenTestQoSResource.class);
        router.attach("/V2/qos/O{open_test_uuid}/{lang}", OpenTestQoSResource.class);
        router.attach("/V3/qos/O{open_test_uuid}", OpenTestQoSResource.class);
        router.attach("/V3/qos/O{open_test_uuid}/{lang}", OpenTestQoSResource.class);

        router.attach("/qosTestRequest", QoSTestRequestResource.class);
        router.attach("/qosTestResult", QoSResultResource.class);
        router.attach("/V2/qosTestRequest", QoSTestRequestResource.class);
        router.attach("/V2/qosTestResult", QoSResultResource.class);
        router.attach("/V3/qosTestRequest", QoSTestRequestResource.class);
        router.attach("/V3/qosTestResult", QoSResultResource.class);

        // off/on net update ( new controller and service are here )
        router.attach("/V4/qosTestRequest", QoSTestOffOnNetRequestResource.class);
        router.attach("/V4/qosTestResult", QoSResultResource.class);

        // administrative resources (access restrictions might be applied to /admin/ 
        router.attach("/admin/qosObjectives", QualityOfServiceExportResource.class);
        router.attach("/admin/loadasnnumbers", LoadASNNumbersResource.class);
        router.attach("/V2/admin/qosObjectives", QualityOfServiceExportResource.class);
        router.attach("/V2/admin/loadasnnumbers", LoadASNNumbersResource.class);
        router.attach("/V3/admin/qosObjectives", QualityOfServiceExportResource.class);
        router.attach("/V3/admin/loadasnnumbers", LoadASNNumbersResource.class);

        router.attach("/admin/measurementServer", MeasurementServerControllerResource.class);
        router.attach("/admin/provider", ProviderAdminControllerResource.class);
        router.attach("/admin/setMeasurementOperator", SetRelationProviderTestServerControllerResource.class);
        router.attach("/admin/ping", PingMeasurementServerControllerResource.class);
        router.attach("/admin/measure", MeasureMeasurementServerControllerResource.class);

        // logging resource
        // deactivated as a possible security threat after discussion with Jozef
//        router.attach("/log", LogResource.class);
//        router.attach("/V2/log", LogResource.class);
//        router.attach("/V3/log", LogResource.class);

        // stores zero measurement results
        router.attach("/zeroMeasurement", ZeroMeasurementResource.class);
        router.attach("/V2/zeroMeasurement", ZeroMeasurementResource.class);
        router.attach("/V3/zeroMeasurement", ZeroMeasurementResource.class);

        router.attach("/jnlp", JNLPResource.class);
        router.attach("/V2/jnlp", JNLPResource.class);
        router.attach("/V3/jnlp", JNLPResource.class);

        router.attach("/submitSurvey", SurveyResource.class);
        router.attach("/V2/submitSurvey", SurveyResource.class);
        router.attach("/V3/submitSurvey", SurveyResource.class);

        router.attach("/checkSurvey", SurveyCheckResource.class);
        router.attach("/V2/checkSurvey", SurveyCheckResource.class);
        router.attach("/V3/checkSurvey", SurveyCheckResource.class);

        router.attach("/advertising", AdvertisingResource.class);
        router.attach("/V2/advertising", AdvertisingResource.class);

        router.attach("/advertisedSpeedOptions", AdvertisedSpeedResource.class);
        router.attach("/V2/advertisedSpeedOptions", AdvertisedSpeedResource.class);
        router.attach("/V3/advertisedSpeedOptions", AdvertisedSpeedResource.class);

        //
        router.attach("/clientRegistration", ClientRegistrationResource.class);
        router.attach("/V2/clientRegistration", ClientRegistrationResource.class);
        router.attach("/V3/clientRegistration", ClientRegistrationResource.class);

        // GDPR legal age
        router.attach("/gdpr", GDPRLegalAgeResource.class);
        router.attach("/V2/gdpr", GDPRLegalAgeResource.class);
        router.attach("/V3/gdpr", GDPRLegalAgeResource.class);

        // Badges
        router.attach("/badges", BadgeResource.class);
        router.attach("/V2/badges", BadgeResource.class);
        router.attach("/V3/badges", BadgeResource.class);

        //Health check
        router.attach("/admin/healthCheck", HealthMonitorResource.class);
        router.attach("/V2/admin/healthCheck", HealthMonitorResource.class);
        router.attach("/V3/admin/healthCheck", HealthMonitorResource.class);

        // Load Badges Resources
        router.attach("/admin/loadBadges", LoadBadgesResource.class);
        router.attach("/V2/admin/loadBadges", LoadBadgesResource.class);
        router.attach("/V3/admin/loadBadges", LoadBadgesResource.class);

        return router;
    }

}
