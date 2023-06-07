/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
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
package at.alladin.rmbt.shared.reporting;

import at.alladin.rmbt.shared.db.QoSTestResult;
import at.alladin.rmbt.shared.db.Test;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AdvancedReporting {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(AdvancedReporting.class);

    private static final String SQL_UPDATE = "UPDATE test SET additional_report_fields = "
            + " (SELECT ('{' || string_agg(to_json(\"key\")::text || ':' ||\"value\", ',') || '}')::jsonb::json FROM "
            + " (SELECT * FROM json_each((SELECT additional_report_fields FROM test WHERE uid=?)::json) "
            + " UNION ALL SELECT * FROM json_each(?::json)) jmerged) WHERE uid = ? ";

    final static List<Class<? extends AdvancedReport>> staticAdvancedReportList = new ArrayList<>();

    @SafeVarargs
    public static void init(Class<? extends AdvancedReport>... advancedReports) {
        Collections.addAll(staticAdvancedReportList, advancedReports);
    }

    public static AdvancedReporting newInstance() throws InstantiationException, IllegalAccessException {
        return new AdvancedReporting();
    }

    final private List<AdvancedReport> advancedReportList;

    private AdvancedReporting() throws InstantiationException, IllegalAccessException {
        this.advancedReportList = new ArrayList<>();
        for (Class<? extends AdvancedReport> clazz : staticAdvancedReportList) {
            advancedReportList.add(clazz.newInstance());
        }
    }

    public JSONObject generateSpeedtestAdvancedReport(final Test test, final JSONObject testResultJson) throws JSONException {
        final JSONObject reportJson = new JSONObject();
        for (final AdvancedReport r : advancedReportList) {
            r.addSpeedtestReport(test, testResultJson, reportJson);
        }

        return reportJson;
    }

    public JSONObject generateQoSAdvancedReport(final List<QoSTestResult> qosResultList) throws JSONException {
        final JSONObject reportJson = new JSONObject();
        for (final AdvancedReport r : advancedReportList) {
            r.addQoSReport(qosResultList, reportJson);
        }

        return reportJson;
    }

    public int updateTest(final long uid, final Connection conn, final JSONObject reportJson) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);

        ps.setLong(1, uid);
        ps.setObject(2, reportJson.toString());
        ps.setLong(3, uid);

        logger.debug(ps.toString());
        return ps.executeUpdate();
    }
}
