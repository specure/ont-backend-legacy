/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package at.alladin.rmbt.mapServer;

import at.alladin.rmbt.shared.Classification;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

final public class MapServerOptions {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(MapServerOptions.class);

    public static MapServerOptions getInstance() {
        return instance;
    }

    public static MapServerOptions getInstance(Classification classification) {
        instance = new MapServerOptions(classification);
        return instance;
    }

    public static void initInstance(Classification classification) {
        instance = new MapServerOptions(classification);
    }

    private static volatile MapServerOptions instance;

    // 10^(a×4)×10
    // log(a÷10)÷4

    // 10^(a×3)×1000000
    // log(a÷1000000)÷3

    protected final int[] colors_ryg =
            new int[]{0x600000, 0xff0000, 0xffff00, 0x00ff00, 0x00cb00, 0x009600, 0x006100};
    protected final int[] colors_ryg_short =
            new int[]{0x600000, 0xff0000, 0xffff00, 0x00ff00, 0x00b000};

    protected final double[] values_download;
    protected final String[] captions_download;

    protected final double[] values_upload;
    protected final String[] captions_upload;

    // protected final double[] values_ping;
    // protected final String[] captions_ping;

    protected final Classification classification;

    private MapServerOptions(Classification classification) {
        this.classification = classification;

        values_download = new double[7];
        captions_download = new String[7];
        calcValues(4, classification.THRESHOLD_DOWNLOAD[1], classification.THRESHOLD_DOWNLOAD[0],
                values_download, captions_download, "Mb/s");

        values_upload = new double[7];
        captions_upload = new String[7];
        calcValues(4, classification.THRESHOLD_UPLOAD[1], classification.THRESHOLD_UPLOAD[0],
                values_upload, captions_upload, "Mb/s");

        // values_ping = new double[7];
        // captions_ping = new String[7];
        // calcValues(3, classification.THRESHOLD_PING[1] / 100000, classification.THRESHOLD_PING[0] /
        // 100000, values_ping, captions_ping);

        mapOptionMap = genMapOptionMap();
    }

    private static void calcValues(final double factor, final double d1, final double d2,
                                   final double[] values, final String[] captions, String Legend) {
        final double d1l = Math.log10(d1 / 10) / factor;
        final double d2l = Math.log10(d2 / 10) / factor;
        final double step = d2l - d1l;
        for (int i = 0; i < values.length; i++) {
            final double val = d1l + (i - 1.5) * step;
            values[i] = val;
            if (i % 2 == 0)
                captions[i] = String.format(Locale.US, "%.1f", Math.pow(10, val * factor) * 10 / 1000); // to
                // mbit;
                // input
                // is
                // kbit
            else
                captions[i] = "";
        }

        captions[values.length - 1] = String.format("[%s]", Legend);

    }

    // protected static final double[] values_download = new double[] { 0.3871137516, 0.4623712505,
    // 0.5376287495, 0.6128862484, 0.6881437473, 0.7634012462, 0.8386587451 };
    // protected static final String[] captions_download = new String[] { "0.4", "", "1.4", "", "5.7",
    // "", "22.6" };

    // protected static final double[] values_upload = new double[] { 0.3118562527, 0.3871137516,
    // 0.4623712505, 0.5376287495, 0.6128862484, 0.6881437473, 0.7634012462 };
    // protected static final String[] captions_upload = new String[] { "0.2", "", "0.7", "", "2.8",
    // "", "11.3" };

    protected final double[] values_ping = new double[]{0.8996566681, 0.7329900014, 0.5663233348,
            0.3996566681, 0.2329900014, 0.0663233348, -0.1003433319};
    protected final String[] captions_ping = new String[]{"500", "", "50", "", "5", "", "[ms]"};

    protected final Map<String, MapOption> mapOptionMap;

    protected LinkedHashMap<String, MapOption> genMapOptionMap() {
        return new LinkedHashMap<String, MapOption>() {
            {
                put("mobile/download", new MapOption("speed_download", "speed_download_log",
                        "speed_download is not null AND (network_type not in (0, 97, 98, 99) OR (network_type not in (97, 98, 99) AND t.zero_measurement = true))",

                        colors_ryg, values_download, captions_download,

                        classification.THRESHOLD_DOWNLOAD, classification.THRESHOLD_DOWNLOAD_CAPTIONS,
                        "heatmap", false));

                put("mobile/upload",
                        new MapOption("speed_upload", "speed_upload_log",
                                "speed_upload is not null AND (network_type not in (0, 97, 98, 99) OR (network_type not in (97, 98, 99) AND t.zero_measurement = true))", colors_ryg,
                                values_upload, captions_upload, classification.THRESHOLD_UPLOAD,
                                classification.THRESHOLD_UPLOAD_CAPTIONS, "heatmap", false));


                put("mobile/ping",
                        new MapOption("ping_median", "ping_median_log",
                                "ping_median is not null AND (network_type not in (0, 97, 98, 99) OR (network_type not in (97, 98, 99) AND t.zero_measurement = true))", colors_ryg,
                                values_ping, captions_ping, classification.THRESHOLD_PING,
                                classification.THRESHOLD_PING_CAPTIONS, "heatmap", true));

                put("mobile/signal",
                        new MapOption("merged_signal",
                                "merged_signal is not null AND (network_type not in (0, 97, 98, 99) OR (network_type not in (97, 98, 99) AND t.zero_measurement = true))", colors_ryg, // colors_ryg_short,
                                new double[]{-123.5, -108.5, -93.5, -78.5, -63.5, -48.5, -33.5},
                                new String[]{"-108", "", "-94", "", "-78", "", "[dBm]"},
                                classification.THRESHOLD_SIGNAL_MOBILE,
                                classification.THRESHOLD_SIGNAL_MOBILE_CAPTIONS, "heatmap", false));

                put("wifi/download",
                        new MapOption("speed_download", "speed_download_log",
                                "speed_download is not null AND network_type = 99", colors_ryg, values_download,
                                captions_download, classification.THRESHOLD_DOWNLOAD,
                                classification.THRESHOLD_DOWNLOAD_CAPTIONS, "heatmap", false));

                put("wifi/upload",
                        new MapOption("speed_upload", "speed_upload_log",
                                "speed_upload is not null AND network_type = 99", colors_ryg, values_upload,
                                captions_upload, classification.THRESHOLD_UPLOAD,
                                classification.THRESHOLD_UPLOAD_CAPTIONS, "heatmap", false));

                put("wifi/ping",
                        new MapOption("ping_median", "ping_median_log",
                                "ping_median is not null AND network_type = 99", colors_ryg, values_ping,
                                captions_ping, classification.THRESHOLD_PING,
                                classification.THRESHOLD_PING_CAPTIONS, "heatmap", true));

                put("wifi/signal",
                        new MapOption("signal_strength", "signal_strength is not null AND network_type = 99",
                                colors_ryg, // colors_ryg_short,
                                new double[]{-98.5, -83.5, -68.5, -53.5, -38.5, -23.5, -8.5},
                                new String[]{"-99", "", "-69", "", "-39", "", "[dBm]"},
                                classification.THRESHOLD_SIGNAL_WIFI, classification.THRESHOLD_SIGNAL_WIFI_CAPTIONS,
                                "heatmap", false));

                put("browser/download",
                        new MapOption("speed_download", "speed_download_log",
                                "speed_download is not null AND network_type = 98", colors_ryg, values_download,
                                captions_download, classification.THRESHOLD_DOWNLOAD,
                                classification.THRESHOLD_DOWNLOAD_CAPTIONS, "shapes", false));

                put("browser/upload",
                        new MapOption("speed_upload", "speed_upload_log",
                                "speed_upload is not null AND network_type = 98", colors_ryg, values_upload,
                                captions_upload, classification.THRESHOLD_UPLOAD,
                                classification.THRESHOLD_UPLOAD_CAPTIONS, "shapes", false));

                put("browser/ping", new MapOption("ping_median", "ping_median_log",
                        "ping_median is not null AND network_type = 98", colors_ryg, values_ping, captions_ping,
                        classification.THRESHOLD_PING, classification.THRESHOLD_PING_CAPTIONS, "shapes", true));

                put("all/download",
                        new MapOption("speed_download", "speed_download_log", "speed_download is not null",
                                colors_ryg, values_download, captions_download, classification.THRESHOLD_DOWNLOAD,
                                classification.THRESHOLD_DOWNLOAD_CAPTIONS, "shapes", false));

                put("all/upload",
                        new MapOption("speed_upload", "speed_upload_log", "speed_upload is not null",
                                colors_ryg, values_upload, captions_upload, classification.THRESHOLD_UPLOAD,
                                classification.THRESHOLD_UPLOAD_CAPTIONS, "shapes", false));

                put("all/ping",
                        new MapOption("ping_median", "ping_median_log", "ping_median is not null", colors_ryg,
                                values_ping, captions_ping, classification.THRESHOLD_PING,
                                classification.THRESHOLD_PING_CAPTIONS, "shapes", true));
            }
        };
    }

    protected final List<SQLFilter> defaultMapFilters =
            Collections.unmodifiableList(new ArrayList<SQLFilter>() {
                {
                    add(new SQLFilter(
                            "t.deleted = false AND (t.implausible = false OR (t.implausible = true AND t.zero_measurement = true)) AND t.status = 'FINISHED'"));
                }
            });

    protected final SQLFilter accuracyMapFilter = new SQLFilter("t.geo_accuracy < 2000"); // 2km

    protected final Map<String, MapFilter> mapFilterMap =
            Collections.unmodifiableMap(new LinkedHashMap<String, MapFilter>() {
                {
                    put("operator", new MapFilter() {
                        @Override
                        SQLFilter getFilter(final String input) {
                            if (Strings.isNullOrEmpty(input))
                                return null;
                            if (input.equals("other"))
                                return new SQLFilter("mobile_provider_id IS NULL");
                            else
                                return new SQLFilter("(mobile_provider_id = ? OR provider_id = ?)") {
                                    @Override
                                    int fillParams(int i, final PreparedStatement ps) throws SQLException {
                                        ps.setInt(i++, Integer.parseInt(input));
                                        ps.setInt(i++, Integer.parseInt(input));
                                        return i;
                                    }
                                };
                        }
                    });

                    put("mobile_provider_name", new MapFilter() {
                        @Override
                        SQLFilter getFilter(final String input) {
                            if (Strings.isNullOrEmpty(input)) {
                                return null;
                            }
                            if(input.equalsIgnoreCase("all")){
                                return null;
                            }

//                             return new SQLFilter("provider_id=?") {
                            return new SQLFilter("((t.network_sim_operator in (select mcc_mnc from provider where shortname = ? and mcc_mnc is not null)) or (t.public_ip_asn in (SELECT ap.asn FROM as2provider ap JOIN provider p ON(ap.provider_id = p.uid) WHERE p.shortname = ?)))") {
                                @Override
                                int fillParams(int i, final PreparedStatement ps) throws SQLException {
                                    ps.setString(i++, input);
                                    ps.setString(i++, input);
                                    return i;
                                }
                            };
                        }
                    });

                    put("provider", new MapFilter() {
                        @Override
                        SQLFilter getFilter(final String input) {
                            if (Strings.isNullOrEmpty(input)) {
                                return null;
                            }
                            if(input.equalsIgnoreCase("all")){
                                return null;
                            }

//                             return new SQLFilter("provider_id=?") {
                                return new SQLFilter("(t.network_operator in (select mcc_mnc from provider where uid = ?) OR t.provider_id = ?)") {
                                    @Override
                                    int fillParams(int i, final PreparedStatement ps) throws SQLException {
                                        ps.setInt(i++, Integer.parseInt(input));
                                        ps.setInt(i++, Integer.parseInt(input));
                                        return i;
                                    }
                                };
                        }
                    });

                    put("technology", new MapFilter() {
                        @Override
                        SQLFilter getFilter(final String input) { // do not filter if empty
                            if (Strings.isNullOrEmpty(input))
                                return null;
                            try {
                                final int technology = Integer.parseInt(input);
                                // use old numeric network type (replicate network_type_table here)
                                if (technology == 2) // 2G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16)");
                                else if (technology == 3) // 3G
                                    return new SQLFilter("network_type in (3,8,9,10,15,17)");
                                else if (technology == 4) // 4G
                                    return new SQLFilter("network_type in (13,19,40)");
                                else if (technology == 5) // 5G
                                    return new SQLFilter("network_type in (20,41) ");
                                else if (technology == 23) // 2G or 3G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,3,8,9,10,15,17)");
                                else if (technology == 24) // 2G or 4G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,13,19,40)");
                                else if (technology == 25) // 2G or 5G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,20,41)");
                                else if (technology == 34) // 3G or 4G
                                    return new SQLFilter("network_type in (3,8,9,10,15,17,13,19,40)");
                                else if (technology == 35) // 3G or 5G
                                    return new SQLFilter("network_type in (3,8,9,10,15,17,20,41)");
                                else if (technology == 45) // 4G or 5G
                                    return new SQLFilter("network_type in (13,19,40,20,41)");
                                else if (technology == 234) // 2G or 3G or 4G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,3,8,9,10,15,17,13,19,40)");
                                else if (technology == 235) // 2G or 3G or 5G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,3,8,9,10,15,17,20,41)");
                                else if (technology == 245) // 2G or 4G or 5G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,13,19,40,20,41)");
                                else if (technology == 345) // 3G or 4G or 5G
                                    return new SQLFilter("network_type in (3,8,9,10,15,17,13,19,40,20,41)");
                                else if (technology == 2345) // 2G or 3G or 4G or 5G
                                    return new SQLFilter("network_type in (1,2,4,5,6,7,11,12,14,16,3,8,9,10,15,17,13,19,40,20,41)");
                                else
                                    return  new SQLFilter("1=1");

                /*
                 * //alternative: use network_group_name return new
                 * SQLFilter("network_group_name=?") {
                 * 
                 * @Override int fillParams(int i, final PreparedStatement ps) throws SQLException {
                 * // convert 2 => '2G' ps.setString(i++, String.format("%dG", technology)); return
                 * i; } };
                 */
                            } catch (NumberFormatException e) {
                                logger.error(e.getMessage());
                                return null;
                            }
                        }
                    });

                    put("period", new MapFilter() {
                        @Override
                        SQLFilter getFilter(final String input) {
                            if (Strings.isNullOrEmpty(input))
                                return null;
                            try {
                                final int period = Integer.parseInt(input);
                                if (period <= 0 || period > 730)
                                    return null;

                                return new SQLFilter("t.time > NOW() - CAST(? AS INTERVAL)") {
                                    @Override
                                    int fillParams(int i, final PreparedStatement ps) throws SQLException {
                                        ps.setString(i++, String.format("%d days", period));
                                        return i;
                                    }
                                };
                            } catch (NumberFormatException e) {
                                logger.error(e.getMessage());
                                return null;
                            }
                        }
                    });

                    put("country", new MapFilter() {
                        @Override
                        SQLFilter getFilter(String input) {
                            if (Strings.isNullOrEmpty(input)) {
                                return null;
                            }
                            if(input.equalsIgnoreCase("all")){
                                return null;
                            }

                            return new SQLFilter("COALESCE(t.country_geoip, t.country_location, t.country_asn) = ? "){
                                @Override
                                int fillParams(int i, final PreparedStatement ps) throws SQLException {
                                    ps.setString(i++, input.toUpperCase());
                                    return i;
                                }
                            };
                        }
                    });

                    // put("device", new MapFilter()
                    // {
                    // @Override
                    // SQLFilter getFilter(final String input)
                    // {
                    // if (Strings.isNullOrEmpty(input))
                    // return null;
                    // final String[] devices = input.split(";");
                    // final StringBuilder builder = new StringBuilder("model IN (");
                    // for (int i = 0; i < devices.length; i++)
                    // {
                    // if (i > 0)
                    // builder.append(',');
                    // builder.append('?');
                    // }
                    // builder.append(')');
                    // return new SQLFilter(builder.toString())
                    // {
                    // @Override
                    // int fillParams(int i, final PreparedStatement ps) throws SQLException
                    // {
                    // for (String device : devices)
                    // ps.setString(i++, device);
                    // return i;
                    // }
                    // };
                    // }
                    // });
                }
            });

    static class MapOption {
        public MapOption(final String valueColumn, final String sqlFilter, final int[] colors,
                         final double[] intervals, final String[] captions, final int[] classification,
                         final String[] classificationCaptions, final String overlayType,
                         final boolean reverseScale) {
            this(valueColumn, valueColumn, sqlFilter, colors, intervals, captions, classification,
                    classificationCaptions, overlayType, reverseScale);
        }

        public MapOption(final String valueColumn, final String valueColumnLog, final String sqlFilter,
                         final int[] colors, final double[] intervals, final String[] captions,
                         final int[] classification, final String[] classificationCaptions, final String overlayType,
                         final boolean reverseScale) {
            super();
            this.valueColumn = valueColumn;
            this.valueColumnLog = valueColumnLog;
            this.sqlFilter = sqlFilter;
            this.intervals = intervals;
            this.captions = captions;
            this.classification = classification;
            this.classificationCaptions = classificationCaptions;
            this.overlayType = overlayType;
            this.reverseScale = reverseScale;


            if (intervals.length != colors.length || intervals.length != captions.length)
                throw new IllegalArgumentException("illegal array size");

            colorsHexStrings = new String[colors.length];
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] < 0 || colors[i] > 0xffffff)
                    throw new IllegalArgumentException("illegal color [" + i + "]: " + colors[i]);
                colorsHexStrings[i] = String.format("#%06x", colors[i]);
            }

            final SortedMap<Double, Integer> sortedIntervals = new TreeMap<>();
            for (int i = 0; i < intervals.length; i++)
                sortedIntervals.put(intervals[i], colors[i]);

            colorsSorted = new int[sortedIntervals.size()];
            intervalsSorted = new double[sortedIntervals.size()];
            int i = 0;
            for (final Map.Entry<Double, Integer> entry : sortedIntervals.entrySet()) {
                intervalsSorted[i] = entry.getKey();
                colorsSorted[i++] = entry.getValue();
            }

        }

        final String valueColumn;
        final String valueColumnLog;
        final String sqlFilter;
        final int[] colorsSorted;
        final double[] intervalsSorted;
        final String[] colorsHexStrings;
        final double[] intervals;
        final String[] captions;
        final int[] classification;
        final String[] classificationCaptions;
        final String overlayType;
        final boolean reverseScale;

        public int getClassification(final long value) {
            return Classification.classify(classification, value);
        }
    }

    static abstract class MapFilter {
        abstract SQLFilter getFilter(String input);
    }

    static class StaticMapFilter extends MapFilter {
        private final SQLFilter filter;

        public StaticMapFilter(String where) {
            filter = new SQLFilter(where);
        }

        @Override
        SQLFilter getFilter(String input) {
            return filter;
        }
    }

    static class SQLFilter {
        public SQLFilter(final String where) {
            this.where = where;
        }

        final String where;

        int fillParams(final int i, final PreparedStatement ps) throws SQLException {
            return i;
        }
    }

    public Map<String, MapOption> getMapOptionMap() {
        return mapOptionMap;
    }

    public Map<String, MapFilter> getMapFilterMap() {
        return mapFilterMap;
    }

    public boolean isValidFilter(String name) {
        return mapFilterMap.containsKey(name);
    }

    public List<SQLFilter> getDefaultMapFilters() {
        return defaultMapFilters;
    }

    public SQLFilter getAccuracyMapFilter() {
        return accuracyMapFilter;
    }

    public static void main(String[] args) {
        Classification.initInstance(null);
        final MapServerOptions mso = new MapServerOptions(Classification.getInstance());
        logger.debug(Arrays.toString(mso.values_download));
        logger.debug(Arrays.toString(mso.captions_download));
        logger.debug("");
        logger.debug(Arrays.toString(mso.values_upload));
        logger.debug(Arrays.toString(mso.captions_upload));
        logger.debug("");
        logger.debug(Arrays.toString(mso.values_ping));
        logger.debug(Arrays.toString(mso.captions_ping));
    }
}
