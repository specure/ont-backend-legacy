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
package at.alladin.rmbt.statisticServer.report;

import at.alladin.rmbt.shared.Helperfunctions;
import at.alladin.rmbt.shared.ResourceManager;
import at.alladin.rmbt.shared.SignificantFormat;
import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.fields.Field;
import at.alladin.rmbt.shared.db.fields.TimestampField;
import at.alladin.rmbt.statisticServer.ServerResource;
import at.alladin.rmbt.statisticServer.report.TestReportUtil.TestReportCategory;
import at.alladin.rmbt.statisticServer.report.TestReportUtil.ValueFormatter;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TestReport extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestReport.class);

    private final TestReportUtil.HyperlinkCellFormat.HyperlinkGenerator hyperlinkGenerator = new TestReportUtil.HyperlinkCellFormat.HyperlinkGenerator() {
        @Override
        public String generate(String value, Field field) {
            try {
                return "https://" + settings.getString("RMBT_URL") + "/en/opentest?" + field.getDbKey() + "=" + URLEncoder.encode(value, "UTF8");
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        }
    };

    public final TestReportUtil.HyperlinkCellFormat.HyperlinkGenerator timeLinkGenerator = new TestReportUtil.HyperlinkCellFormat.HyperlinkGenerator() {
        @Override
        public String generate(String value, Field field) {
            try {
                final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                final String dateString = format.format(((TimestampField) field).getDate());
                final String timeUrlEncodedG = URLEncoder.encode("time[]>", "UTF8");
                final String timeUrlEncodedL = URLEncoder.encode("time[]<", "UTF8");
                return "https://" + settings.getString("RMBT_URL") + "/en/opentest?" + timeUrlEncodedG + "=" + URLEncoder.encode(dateString + " 00:00:00", "UTF8") +
                        "&" + timeUrlEncodedL + "=" + URLEncoder.encode(dateString + " 23:59:59", "UTF8");
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        }
    };

    @Get
    public Representation request(String request) {
        try {
            final Test t = new Test(conn);

            String queryLang = (String) getRequest().getAttributes().get("lang");
            final List<String> langs = Arrays.asList(settings.getString("RMBT_SUPPORTED_LANGUAGES").split(",\\s*"));
            if (!langs.contains(queryLang)) {
                queryLang = settings.getString("RMBT_DEFAULT_LANGUAGE");
            } else {
                labels = ResourceManager.getSysMsgBundle(new Locale(queryLang));
            }

            final String lang = queryLang;

            final Locale locale = new Locale(lang);
            final Format format = new SignificantFormat(2, locale);

            final UUID openUUID = UUID.fromString(getRequest().getAttributes().get("open_test_uuid").toString());
            t.getTestByOpenTestUuid(openUUID);

            final OutputRepresentation result = new OutputRepresentation(MediaType.APPLICATION_MSOFFICE_XLSX) {

                @Override
                public void write(OutputStream out) throws IOException {
                    try {
                        final List<TestReportCategory> reportList = new ArrayList<TestReportCategory>();

                        reportList.add(new TestReportCategory(new Point(1, 1), "RESULT_MEASUREMENT",
                                new TestReportUtil.TestReportField<Test>("key_speed_download",
                                        new TestReportUtil.FormattedReportCell<Test>("speed_download", "%s %s", labels.getString("RESULT_DOWNLOAD_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000d);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_speed_upload",
                                        new TestReportUtil.FormattedReportCell<Test>("speed_upload", "%s %s", labels.getString("RESULT_UPLOAD_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000d);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_ping_median",
                                        new TestReportUtil.FormattedReportCell<Test>("ping_median", "%s %s", labels.getString("RESULT_PING_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000000d);
                                                    }
                                                }))));

                        reportList.add(new TestReportCategory(new Point(1, 8), "RESULT_DETAILS",
                                new TestReportUtil.TestReportField<Test>("key_time",
                                        new TestReportUtil.StringReportCell<Test>("time",
                                                new TestReportUtil.HyperlinkCellFormat(timeLinkGenerator))),
                                new TestReportUtil.TestReportField<Test>("key_timezone", "timezone"),
                                new TestReportUtil.TestReportField<Test>("key_network_type",
                                        new TestReportUtil.StringReportCell<Test>("network_type", null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(Field value) {
                                                        //return Helperfunctions.getNetworkTypeName(value.intValue());
                                                        return Helperfunctions.getNetworkTypeNameTranslated(value.intValue(), lang, labels);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_country_asn", "country_asn"),
                                // removed by request of the customer - SDNT-176
                                /*
                                new TestReportUtil.TestReportField<Test>("key_country_geoip",
										new TestReportUtil.StringReportCell<Test>("country_geoip", 
												new TestReportUtil.HyperlinkCellFormat(hyperlinkGenerator))),
								*/
                                new TestReportUtil.TestReportField<Test>("key_speed_test_duration",
                                        new TestReportUtil.FormattedReportCell<Test>("speed_test_duration", "%s %s", labels.getString("RESULT_DURATION_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000d);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_client_public_ip", "client_public_ip_anonymized"),
                                new TestReportUtil.TestReportField<Test>("key_client_public_ip_as", "public_ip_asn"),
                                new TestReportUtil.TestReportField<Test>("key_client_public_ip_as_name",
                                        new TestReportUtil.StringReportCell<Test>("public_ip_as_name",
                                                new TestReportUtil.HyperlinkCellFormat(hyperlinkGenerator))),
                                new TestReportUtil.TestReportField<Test>("key_duration_dl",
                                        new TestReportUtil.FormattedReportCell<Test>("nsec_download", "%s %s", labels.getString("RESULT_DURATION_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000000000d);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_duration_ul",
                                        new TestReportUtil.FormattedReportCell<Test>("nsec_upload", "%s %s", labels.getString("RESULT_DURATION_UNIT"), null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(final Field value) {
                                                        return format.format(value.doubleValue() / 1000000000d);
                                                    }
                                                })),
                                new TestReportUtil.TestReportField<Test>("key_server_name", "server_name"),
                                new TestReportUtil.TestReportField<Test>("key_model",
                                        new TestReportUtil.StringReportCell<Test>("model",
                                                new TestReportUtil.HyperlinkCellFormat(hyperlinkGenerator))),
                                // removed by request of the customer - SDNT-176
								/*
								new TestReportUtil.TestReportField<Test>("key_client_name", "client_name"),
								new TestReportUtil.TestReportField<Test>("key_client_software_version", "client_software_version"),
								new TestReportUtil.TestReportField<Test>("key_client_version", 
										new TestReportUtil.StringReportCell<Test>("client_version", 
												new TestReportUtil.HyperlinkCellFormat(hyperlinkGenerator))),
								new TestReportUtil.TestReportField<Test>("key_duration", 
										new TestReportUtil.FormattedReportCell<Test>("duration", "%d %s", labels.getString("RESULT_DURATION_UNIT"), null,
												new ValueFormatter() {
													@Override
													public Object format(final Field value) {
														return value.intValue();
													}
												})),
								*/
                                new TestReportUtil.TestReportField<Test>("key_num_threads", "num_threads"),
                                new TestReportUtil.TestReportField<Test>("key_open_test_uuid",
                                        new TestReportUtil.StringReportCell<Test>("open_test_uuid", null,
                                                new ValueFormatter() {
                                                    @Override
                                                    public Object format(Field value) {
                                                        return "O" + value.toString();
                                                    }
                                                }))
                        ));


                        TestReportUtil.getTestReport(t, reportList, labels, lang).write(out);
                    } catch (final Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            };

            logger.debug(t.getError());

            if (!t.hasError()) {
                final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
                disposition.setFilename("O" + openUUID + "_" + lang + ".xlsx");
                result.setDisposition(disposition);
                return result;
            } else {
                return null;
            }
        } catch (final Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
