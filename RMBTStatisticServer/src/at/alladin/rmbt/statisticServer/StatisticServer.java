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
package at.alladin.rmbt.statisticServer;

import at.alladin.rmbt.statisticServer.export.*;
import at.alladin.rmbt.statisticServer.exportraw.ExportRawResource;
import at.alladin.rmbt.statisticServer.report.TestReport;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

public class StatisticServer extends Application {

    /**
     * Public Constructor to create an instance of DemoApplication.
     *
     * @param parentContext - the org.restlet.Context instance
     */
    public StatisticServer(final Context parentContext) {
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

        router.attach("/statistics", StatisticsResource.class);
        router.attach("/V2/statistics", StatisticsResource.class);
        router.attach("/V3/statistics", StatisticsResource.class);

        router.attach("/export/O{open_test_uuid}", TestExportResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/O{open_test_uuid}", TestExportResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/O{open_test_uuid}", TestExportResource.class, Template.MODE_STARTS_WITH);

        //router.attach("/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-{year}-{month}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-{year}-{month}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-{year}-{month}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-{year}-{month}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata-{year}-{month}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export/nettest-opendata", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/export", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/V2/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/V2/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-{year}-{month}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-{year}-{month}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-{year}-{month}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-{year}-{month}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata-{year}-{month}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export/nettest-opendata", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/export", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/V3/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        //router.attach("/V3/export/nettest-opendata-from-{yearFrom}-{monthFrom}-{dayFrom}-to-{yearTo}-{monthTo}-{dayTo}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-{year}-{month}.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-{year}-{month}.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-{year}-{month}.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-{year}-{month}.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata-{year}-{month}", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata.zip", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata.csv", ExportCSVResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata.json", ExportJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata.xml", ExportXMLResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export/nettest-opendata", ExportPureJSONResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/export", ExportCSVResource.class, Template.MODE_STARTS_WITH);

//        router.attach("/{lang}/{open_test_uuid}/{size}.png", ImageExport.class);

        // administrative resources (access restrictions might be applied to /admin/ 

        router.attach("/admin/exportraw/nettest-rawdata-{year}-{month}.", ExportRawResource.class, Template.MODE_STARTS_WITH);
        router.attach("/admin/exportraw", ExportRawResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/admin/exportraw/nettest-rawdata-{year}-{month}.", ExportRawResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/admin/exportraw", ExportRawResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/admin/exportraw/nettest-rawdata-{year}-{month}.", ExportRawResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/admin/exportraw", ExportRawResource.class, Template.MODE_STARTS_WITH);


        router.attach("/opentests", OpenTestSearchResource.class);
        router.attach("/V2/opentests", OpenTestSearchResource.class);
        router.attach("/V3/opentests", OpenTestSearchResource.class);

        router.attach("/opentests/histogra{histogram}", OpenTestSearchResource.class);
        router.attach("/V2/opentests/histogra{histogram}", OpenTestSearchResource.class);
        router.attach("/V3/opentests/histogra{histogram}", OpenTestSearchResource.class);

        router.attach("/opentests/search", OpenTestSearchResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V2/opentests/search", OpenTestSearchResource.class, Template.MODE_STARTS_WITH);
        router.attach("/V3/opentests/search", OpenTestSearchResource.class, Template.MODE_STARTS_WITH);

        router.attach("/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/opentests/O{open_test_uuid}", OpenTestResource.class);
        router.attach("/V2/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/V2/opentests/O{open_test_uuid}", OpenTestResource.class);
        router.attach("/V3/opentests/O{open_test_uuid}&sender={sender}", OpenTestResource.class);
        router.attach("/V3/opentests/O{open_test_uuid}", OpenTestResource.class);

        router.attach("/admin/usage/{period_type}", UsageResource.class);
        router.attach("/admin/usage/{country}", UsageResource.class);
        router.attach("/admin/usage", UsageResource.class);
        router.attach("/admin/usageJSON", UsageJSONResource.class);
        router.attach("/V2/admin/usage/{period_type}", UsageResource.class);
        router.attach("/V2/admin/usage/{country}", UsageResource.class);
        router.attach("/V2/admin/usage", UsageResource.class);
        router.attach("/V2/admin/usageJSON", UsageJSONResource.class);
        router.attach("/V3/admin/usage/{period_type}", UsageResource.class);
        router.attach("/V3/admin/usage/{country}", UsageResource.class);
        router.attach("/V3/admin/usage", UsageResource.class);
        router.attach("/V3/admin/usageJSON", UsageJSONResource.class);

        router.attach("/report/test/O{open_test_uuid}_{lang}.xlsx", TestReport.class);
        router.attach("/V2/report/test/O{open_test_uuid}_{lang}.xlsx", TestReport.class);
        router.attach("/V3/report/test/O{open_test_uuid}_{lang}.xlsx", TestReport.class);

        router.attach("/progress", SumProgress.class);
        router.attach("/V2/progress", SumProgress.class);
        router.attach("/V3/progress", SumProgress.class);

        return router;
    }

}
