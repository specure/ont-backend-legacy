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

import at.alladin.rmbt.shared.SQLHelper;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static at.alladin.rmbt.statisticServer.UsagePeriod.COUNTRY;

//Statistics for internal purpose
//breaks the mvvm-pattern

public class UsageResource extends ServerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(UsageResource.class);

    private String webRoot;
    //    LinkedHashMap<String, StatisticData> statisticData = new LinkedHashMap<>();
//    LinkedHashMap<Date, LinkedHashMap<String, StatisticData>> statisticData = new LinkedHashMap<>();
    SortedMap<Date, LinkedHashMap<String, StatisticData>> statisticData = new TreeMap<>(new Comparator<Date>() {
        @Override
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    });

    private static final String SELECT_DAILY_STATISTICS_FOR_LAST_30_DAYS = "select date_trunc('day', time) _day, %s from test where %s AND time > current_date - interval '30 days' group by _day";
    private static final String SELECT_DAILY_STATISTICS_FOR_LAST_365_DAYS = "select date_trunc('day', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day";
    private static final String SELECT_WEEKLY_STATISTICS_FOR_LAST_365_DAYS = "select date_trunc('week', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day";
    private static final String SELECT_MONTHLY_STATISTICS_FOR_LAST_365_DAYS = "select date_trunc('month', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day";
    private static final String SELECT_YEARLY_STATISTICS_FOR_LAST_365_DAYS = "select date_trunc('year', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day";

    private static final String SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_LOCATION = "select date_trunc('week', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day, country_location ORDER BY _day DESC";
    private static final String SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_ASN = "select date_trunc('week', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day, country_asn ORDER BY _day DESC";
    private static final String SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_GEOIP = "select date_trunc('week', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day, country_geoip ORDER BY _day DESC";
    private static final String SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_NETWORK_GROUP_NAME = "select date_trunc('week', time) _day, %s from test where %s AND time > current_date - interval '365 days' group by _day, network_group_name ORDER BY _day DESC";
    private static final String SELECT_COUNTRY_NETWORK_GROUP_NAME = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips, network_group_name as co_local";;
    private static final String SELECT_COUNTRY_COUNTRY_LOCATION = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips, country_location as co_local";
    private static final String SELECT_COUNTRY_COUNTRY_ASN = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips, country_asn as co_local";
    private static final String SELECT_COUNTRY_COUNTRY_GEOIP = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips, country_geoip as co_local";
    private static final String WHERE_COUNTRY_NETWORK_GROUP_NAME = "status='FINISHED' AND (country_location IS NULL AND country_geoip IS NULL AND country_asn IS NULL AND network_group_name LIKE 'CLI') AND deleted=false and opendata_source ISNULL";
    private static final String WHERE_COUNTRY_LOCATION = "status='FINISHED' AND country_location IS NOT NULL AND country_geoip IS NOT NULL AND deleted=false and opendata_source ISNULL AND time > current_date - interval '365 days'";
    private static final String WHERE_COUNTRY_ASN = "status='FINISHED' AND country_location IS NULL AND country_geoip IS NULL AND country_asn IS NOT NULL AND deleted=false and opendata_source ISNULL AND time > current_date - interval '365 days'";
    private static final String WHERE_COUNTRY_GEOIP = "status='FINISHED' AND country_geoip IS NOT NULL AND country_geoip != '--'  AND deleted=false and opendata_source ISNULL AND time > current_date - interval '365 days'";


    private static final String SELECT = "select FirstSet._day, FirstSet.count_tests, FirstSet.count_clients, FirstSet.count_ips, SecondSet.count_imported_tests from (%s) as FirstSet LEFT OUTER JOIN (%s) as SecondSet on FirstSet._day = SecondSet._day  ORDER by FirstSet._day DESC";
    private static final String SELECT_TOTAL_COUNT = "select FirstSet.count_tests, FirstSet.count_clients, FirstSet.count_ips, SecondSet.count_imported_tests from (select %s from test where %s) as FirstSet LEFT OUTER JOIN (select %s from test where %s) as SecondSet on 1=1";
    private static final String SELECT_FIRST_SET = "count(uid) count_tests, count(DISTINCT client_id) count_clients, count(DISTINCT client_public_ip) count_ips";
    private static final String SELECT_SECOND_SET = "count(uid) count_imported_tests";
    private static final String WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS = "status='FINISHED' AND deleted=false and opendata_source ISNULL ";
    private static final String WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS = "status='FINISHED' AND deleted=false and opendata_source IS NOT NULL ";
    private static final String WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_30_DAYS = "status='FINISHED' AND deleted=false and opendata_source ISNULL ";
    private static final String WHERE_TEST_IS_IMPORTED_FOR_LAST_30_DAYS = "status='FINISHED' AND deleted=false and opendata_source IS NOT NULL ";

    private static final String KEY_COUNTRY = "co_local";
    private static final String KEY_COUNT_CLIENTS = "count_clients";
    private static final String KEY_COUNT_IPS = "count_ips";
    private static final String KEY_COUNT_TESTS = "count_tests";
    private static final String KEY_DAY_FROM = "_day";


    //"http://www.netztest.at/en";
    @Get("html")
    public String request(final String entity) {

        // log request
        logger.debug("rquest: " +entity);

        webRoot = settings.getString("RMBT_URL");
        final StringBuilder result = new StringBuilder();

        try {
            PreparedStatement ps;
            ResultSet rs;
            String sql;
            String sqlFirstSet;
            String sqlSecondSet;
            List<Map.Entry<Long, Long>> statTests = new ArrayList<>();
            List<Map.Entry<Long, Long>> statClients = new ArrayList<>();
            List<Map.Entry<Long, Long>> statIPs = new ArrayList<>();
            List<Map.Entry<Long, Long>> statImportedTests = new ArrayList<>();

            //select statistics for last 30 days

            UsagePeriod usagePeriod;
            if (getRequest().getAttributes().get("period_type") != null) {

                usagePeriod = UsagePeriod.getValue(getRequest().getAttributes().get("period_type").toString().toLowerCase());
                switch (usagePeriod) {
                    case DAY:
                        // daily statistics for last 365 days
                        sqlFirstSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_365_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS);
                        sqlSecondSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_365_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS);
                        sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
                        break;
                    case WEEK:
                        // weekly statistics for last 365 days
                        sqlFirstSet = String.format(SELECT_WEEKLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS);
                        sqlSecondSet = String.format(SELECT_WEEKLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS);
                        sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
                        break;
                    case MONTH:
                        // monthly statistics for last 365 days
                        sqlFirstSet = String.format(SELECT_MONTHLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS);
                        sqlSecondSet = String.format(SELECT_MONTHLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS);
                        sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
                        break;
                    case YEAR:
                        // yearly statistics for last 365 days
                        sqlFirstSet = String.format(SELECT_YEARLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS);
                        sqlSecondSet = String.format(SELECT_YEARLY_STATISTICS_FOR_LAST_365_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS);
                        sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
                        break;
                    default:
                        sqlFirstSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_30_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_30_DAYS);
                        sqlSecondSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_30_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_30_DAYS);
                        sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
                }
            } else {
                // daily statistics just for last 30 days
                usagePeriod = UsagePeriod.UNKNOWN;
                sqlFirstSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_30_DAYS, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_30_DAYS);
                sqlSecondSet = String.format(SELECT_DAILY_STATISTICS_FOR_LAST_30_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_30_DAYS);
                sql = String.format(SELECT, sqlFirstSet, sqlSecondSet);
            }

            if (usagePeriod == COUNTRY) {
                readStatisticData();

                result.append("<html>");
                result.append("<head>");
                result.append("<style>");
                result.append("table {\n" +
                        "    border-collapse: collapse;\n" +
                        "    width: 100%;\n" +
                        "}\n" +
                        "\n" +
                        "td, th {\n" +
                        "    border: 1px solid black;\n" +
                        "    text-align: left;\n" +
                        "    padding: 8px;\n" +
                        "}\n");
                result.append("</style>");
                result.append("</head>");
                result.append("<center><h1> Usage Statistics - Weekly Period for country </h1></center>");

                result.append("<table style=\"width:75%\">");
                result.append("<tr>");
                result.append("<th><b><center>Date</center></b></th>");
                result.append("<th><b><center>Country</center></b></th>");
                result.append("<th><b><center>Tests</center></b></th>");
                result.append("<th><b><center>Clients</center></b></th>");
                result.append("<th><b><center>IPs</center></b></th>");
                result.append("</tr>");
                LinkedHashMap<String, StatisticData> hd = null;
                StatisticData sd = null;
                int columSize = 0;
                TreeSet<StatisticData> tree = null;
                boolean needMerge = true;
                for (Date keyDate : statisticData.keySet()) {
                    hd = statisticData.get(keyDate);
                    tree = new TreeSet(hd.values());
                    needMerge = true;
                    Iterator<StatisticData> it = tree.iterator();
                    while (it.hasNext()) {
                        sd = (StatisticData) it.next();
                        columSize = tree.size();
                        result.append("<tr>");

                        if (needMerge) {
                            result.append("<th rowspan=\"" + columSize + "\"> " + keyDate + "</th>");
                            System.out.println("Merge true" + sd.getDayFrom());
                            needMerge = false;
                        }

                        result.append("<th>" + sd.getCountry() + "</th>");
                        result.append("<th>" + sd.getCountTests() + "</th>");
                        result.append("<th>" + sd.getCountClients() + "</th>");
                        result.append("<th>" + sd.getCountIPs() + "</th>");
                        result.append("</tr>");
                    }
                }
                result.append("</table>");
                result.append("</html>");
            } else {

                ps = conn.prepareStatement(sql);
                logger.debug(ps.toString());
                ps.execute();

                result.append(getHeader(usagePeriod));
                result.append("<thead><tr><th>Date</th><th class=\"r\">#tests</th><th class=\"r\">#clients</th><th class=\"r\">#ips</th><th class=\"r\">#imported_tests</th></tr></thead>\n");
                result.append("<tbody>\n");

                rs = ps.getResultSet();
                while (rs.next()) {
                    final Date dayFrom = rs.getDate("_day");
                    final long countTests = rs.getLong("count_tests");
                    final long countClients = rs.getLong("count_clients");
                    final long countIPs = rs.getLong("count_ips");
                    final long countImportedTests = rs.getLong("count_imported_tests");

                    //TODO
                    final Date dayUntil;
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dayFrom.getTime());

                    switch (usagePeriod) {
                        case DAY:
                            calendar.add(Calendar.DATE, 0);
                            break;
                        case WEEK:
                            calendar.add(Calendar.DATE, 6);
                            break;
                        case MONTH:
                            calendar.add(Calendar.MONTH, 1);
                            calendar.add(Calendar.DATE, -1);
                            break;
                        case YEAR:
                            calendar.add(Calendar.YEAR, 1);
                            calendar.add(Calendar.DATE, -1);
                            break;
                        default:
                            calendar.add(Calendar.DATE, 0);
                    }
                    dayUntil = new Date(calendar.getTimeInMillis());

                    //https://www.meracinternetu.sk/sk/search?time%5B%5D=%3E2016-12-30%2000:00:00&time%5B%5D=%3C2016-12-30%2023:59:59
                    //final String searchlink ="http://"+ webRoot + "/en/search?time%5B%5D=>" + day + "%2000:00:00&amp;time%5B%5D=<" + day + "%2023:59:59";
                    final String searchlink = "http://" + webRoot + "/en/search?time%5B%5D=>" + dayFrom + "%2000:00:00&amp;time%5B%5D=<" + dayUntil + "%2023:59:59";

                    statTests.add(new AbstractMap.SimpleEntry<>(dayFrom.getTime(), countTests));
                    statClients.add(new AbstractMap.SimpleEntry<>(dayFrom.getTime(), countClients));
                    statIPs.add(new AbstractMap.SimpleEntry<>(dayFrom.getTime(), countIPs));
                    statImportedTests.add(new AbstractMap.SimpleEntry<>(dayFrom.getTime(), countImportedTests));
                    //result.append(String.format("%s: % 8d  % 8d  %8d\n", day, countTests, countClients, countIPs));

                    switch (usagePeriod) {
                        case DAY:
                        case UNKNOWN:
                            result.append(String.format("<tr><td><a href=\"%s\">%s</a></td> <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td> <td class=\"r\">%8d</td> </tr>\n", searchlink, dayFrom, countTests, countClients, countIPs, countImportedTests));
                            break;
                        default:
                            result.append(String.format("<tr><td><a href=\"%s\">%s - %s</a></td> <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td>  <td class=\"r\">%8d</td> <td class=\"r\">%8d</td></tr>\n", searchlink, dayFrom, dayUntil, countTests, countClients, countIPs, countImportedTests));
                            calendar.add(Calendar.DATE, 1);
                    }

                }

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);


                switch (usagePeriod) {
                    case DAY:
                    case WEEK:
                    case MONTH:
                    case YEAR:
                        // count of tests for last 365 days
                        sql = String.format(SELECT_TOTAL_COUNT, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_365_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_365_DAYS);
                        break;
                    default:
                        // count of tests just for last 30 days
                        sql = String.format(SELECT_TOTAL_COUNT, SELECT_FIRST_SET, WHERE_TEST_IS_NOT_IMPORTED_FOR_LAST_30_DAYS, SELECT_SECOND_SET, WHERE_TEST_IS_IMPORTED_FOR_LAST_30_DAYS);
                }
                ps = conn.prepareStatement(sql);
                logger.debug(ps.toString());
                ps.execute();

                result.append("\n");

                rs = ps.getResultSet();
                if (rs.next()) {
                    final long countTests = rs.getLong("count_tests");
                    final long countClients = rs.getLong("count_clients");
                    final long countIPs = rs.getLong("count_ips");
                    final long countImportedTests = rs.getLong("count_imported_tests");
                    result.append(String.format("<tr class=\"info\"><td>Total</td><td class=\"r\">%8d</td><td class=\"r\">%8d</td><td class=\"r\">%8d</td><td class=\"r\">%8d</td></tr>\n", countTests, countClients, countIPs, countImportedTests));
                }

                //remove the last day since the day is not yet over (=first in the array)
                if (statTests.size() > 0) {
                    statTests.remove(0);
                    statClients.remove(0);
                    statIPs.remove(0);
                    statImportedTests.remove(0);
                }

                result.append("</tbody></table><div id='flot' style='height:450px;width:100%'></div></div>").append(makeStat(statTests, statClients, statIPs, statImportedTests)).append(getFooter());

                // close prepared statement
                SQLHelper.closePreparedStatement(ps);
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        // log response
        logger.debug("rsponse: " + result.toString());

        return result.toString();
    }

    /**
     * Generates the javascript-code necessary for generating a flot diagram
     *
     * @param tests   list of test count with corresponding timestamp
     * @param clients
     * @param ips
     * @return the generated string from < script> to </ script>
     */
    private static String makeStat(List<Map.Entry<Long, Long>> tests, List<Map.Entry<Long, Long>> clients, List<Map.Entry<Long, Long>> ips, List<Map.Entry<Long, Long>> importedTests) {
        //generate arrays for javascript in form [[11,1],[12,2],...]
        String t = "[";
        for (Map.Entry<Long, Long> entry : tests) {
            t += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        t = t.substring(0, t.length() - 1); //trim last comma
        t += "]";

        String c = "[";
        for (Map.Entry<Long, Long> entry : clients) {
            c += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        c = c.substring(0, c.length() - 1);
        c += "]";

        String i = "[";
        for (Map.Entry<Long, Long> entry : ips) {
            i += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        i = i.substring(0, i.length() - 1);
        i += "]";

        String it = "[";
        for (Map.Entry<Long, Long> entry : importedTests) {
            it += "[" + entry.getKey() + "," + entry.getValue() + "],";
        }
        it = it.substring(0, it.length() - 1); //trim last comma
        it += "]";


        String ret = "<script type=\"text/javascript\">\n" +
                "        var t = " + t + ";\n" +
                "        var c = " + c + ";\n" +
                "        var i = " + i + ";\n" +
                "        var it = " + it + ";\n" +
                "        $(document).ready(function() {\n" +
                "            $.plot(\"#flot\", [{data: t, label: 'Tests'},{data: c, label: 'Clients'},{data: i,label:'IPs'},{data: it,label:'Imported tests'}], {\n" +
                "                        xaxis: {\n" +
                "                                mode: \"time\",\n" +
                "                                minTickSize: [1, \"day\"],\n" +
                "                                timeformat: \"%d.%m.\"\n" +
                "                        }\n" +
                "                }); \n" +
                "        })\n" +
                "\n" +
                "                \n" +
                "\n" +
                "    </script>";

        return ret;
    }

    /**
     * Generates the header including bootstrap, jquery, flot and flot.time
     *
     * @return header from doctype to body
     */
    private String getHeader(UsagePeriod usagePeriod) {
        String usageStatistics;
        switch (usagePeriod) {
            case DAY: {
                usageStatistics = "Usage Statistics - Daily Period";
                break;
            }
            case WEEK: {
                usageStatistics = "Usage Statistics - Weekly Period";
                break;
            }
            case MONTH: {
                usageStatistics = "Usage Statistics - Monthly Period";
                break;
            }
            case YEAR: {
                usageStatistics = "Usage Statistics - Yearly Period";
                break;
            }
            default: {
                usageStatistics = "Usage Statistics - Daily Period";
                break;
            }
        }
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <style type=\"text/css\">\n" + getCSS() + "</style>\n" +
                "    <title>Usage Statistics</title>" +
                "       <!-- jQuery -->\n" +
                "    <script type=\"text/javascript\" src=\"https://" + settings.getString("RMBT_URL") + "/js/jquery-1.8.2.min.js\"></script>\n" +
                "    <!-- FLOT (flotcharts.org -->\n" +
                "    <script type=\"text/javascript\" src=\"https://" + settings.getString("RMBT_URL") + "/js/jquery.flot.min.js\"></script>\n" +
                "    <!-- FLOT time -->\n" +
                "    <script type=\"text/javascript\">\n" +
                "        (function(e){function n(e,t){return t*Math.floor(e/t)}function r(e,t,n,r){if(typeof e.strftime==\"function\")return e.strftime(t);var i=function(e,t){return e=\"\"+e,t=\"\"+(t==null?\"0\":t),e.length==1?t+e:e},s=[],o=!1,u=e.getHours(),a=u<12;n==null&&(n=[\"Jan\",\"Feb\",\"Mar\",\"Apr\",\"May\",\"Jun\",\"Jul\",\"Aug\",\"Sep\",\"Oct\",\"Nov\",\"Dec\"]),r==null&&(r=[\"Sun\",\"Mon\",\"Tue\",\"Wed\",\"Thu\",\"Fri\",\"Sat\"]);var f;u>12?f=u-12:u==0?f=12:f=u;for(var l=0;l<t.length;++l){var c=t.charAt(l);if(o){switch(c){case\"a\":c=\"\"+r[e.getDay()];break;case\"b\":c=\"\"+n[e.getMonth()];break;case\"d\":c=i(e.getDate());break;case\"e\":c=i(e.getDate(),\" \");break;case\"h\":case\"H\":c=i(u);break;case\"I\":c=i(f);break;case\"l\":c=i(f,\" \");break;case\"m\":c=i(e.getMonth()+1);break;case\"M\":c=i(e.getMinutes());break;case\"q\":c=\"\"+(Math.floor(e.getMonth()/3)+1);break;case\"S\":c=i(e.getSeconds());break;case\"y\":c=i(e.getFullYear()%100);break;case\"Y\":c=\"\"+e.getFullYear();break;case\"p\":c=a?\"am\":\"pm\";break;case\"P\":c=a?\"AM\":\"PM\";break;case\"w\":c=\"\"+e.getDay()}s.push(c),o=!1}else c==\"%\"?o=!0:s.push(c)}return s.join(\"\")}function i(e){function t(e,t,n,r){e[t]=function(){return n[r].apply(n,arguments)}}var n={date:e};e.strftime!=undefined&&t(n,\"strftime\",e,\"strftime\"),t(n,\"getTime\",e,\"getTime\"),t(n,\"setTime\",e,\"setTime\");var r=[\"Date\",\"Day\",\"FullYear\",\"Hours\",\"Milliseconds\",\"Minutes\",\"Month\",\"Seconds\"];for(var i=0;i<r.length;i++)t(n,\"get\"+r[i],e,\"getUTC\"+r[i]),t(n,\"set\"+r[i],e,\"setUTC\"+r[i]);return n}function s(e,t){if(t.timezone==\"browser\")return new Date(e);if(!t.timezone||t.timezone==\"utc\")return i(new Date(e));if(typeof timezoneJS!=\"undefined\"&&typeof timezoneJS.Date!=\"undefined\"){var n=new timezoneJS.Date;return n.setTimezone(t.timezone),n.setTime(e),n}return i(new Date(e))}function l(t){t.hooks.processOptions.push(function(t,i){e.each(t.getAxes(),function(e,t){var i=t.options;i.mode==\"time\"&&(t.tickGenerator=function(e){var t=[],r=s(e.min,i),u=0,l=i.tickSize&&i.tickSize[1]===\"quarter\"||i.minTickSize&&i.minTickSize[1]===\"quarter\"?f:a;i.minTickSize!=null&&(typeof i.tickSize==\"number\"?u=i.tickSize:u=i.minTickSize[0]*o[i.minTickSize[1]]);for(var c=0;c<l.length-1;++c)if(e.delta<(l[c][0]*o[l[c][1]]+l[c+1][0]*o[l[c+1][1]])/2&&l[c][0]*o[l[c][1]]>=u)break;var h=l[c][0],p=l[c][1];if(p==\"year\"){if(i.minTickSize!=null&&i.minTickSize[1]==\"year\")h=Math.floor(i.minTickSize[0]);else{var d=Math.pow(10,Math.floor(Math.log(e.delta/o.year)/Math.LN10)),v=e.delta/o.year/d;v<1.5?h=1:v<3?h=2:v<7.5?h=5:h=10,h*=d}h<1&&(h=1)}e.tickSize=i.tickSize||[h,p];var m=e.tickSize[0];p=e.tickSize[1];var g=m*o[p];p==\"second\"?r.setSeconds(n(r.getSeconds(),m)):p==\"minute\"?r.setMinutes(n(r.getMinutes(),m)):p==\"hour\"?r.setHours(n(r.getHours(),m)):p==\"month\"?r.setMonth(n(r.getMonth(),m)):p==\"quarter\"?r.setMonth(3*n(r.getMonth()/3,m)):p==\"year\"&&r.setFullYear(n(r.getFullYear(),m)),r.setMilliseconds(0),g>=o.minute&&r.setSeconds(0),g>=o.hour&&r.setMinutes(0),g>=o.day&&r.setHours(0),g>=o.day*4&&r.setDate(1),g>=o.month*2&&r.setMonth(n(r.getMonth(),3)),g>=o.quarter*2&&r.setMonth(n(r.getMonth(),6)),g>=o.year&&r.setMonth(0);var y=0,b=Number.NaN,w;do{w=b,b=r.getTime(),t.push(b);if(p==\"month\"||p==\"quarter\")if(m<1){r.setDate(1);var E=r.getTime();r.setMonth(r.getMonth()+(p==\"quarter\"?3:1));var S=r.getTime();r.setTime(b+y*o.hour+(S-E)*m),y=r.getHours(),r.setHours(0)}else r.setMonth(r.getMonth()+m*(p==\"quarter\"?3:1));else p==\"year\"?r.setFullYear(r.getFullYear()+m):r.setTime(b+g)}while(b<e.max&&b!=w);return t},t.tickFormatter=function(e,t){var n=s(e,t.options);if(i.timeformat!=null)return r(n,i.timeformat,i.monthNames,i.dayNames);var u=t.options.tickSize&&t.options.tickSize[1]==\"quarter\"||t.options.minTickSize&&t.options.minTickSize[1]==\"quarter\",a=t.tickSize[0]*o[t.tickSize[1]],f=t.max-t.min,l=i.twelveHourClock?\" %p\":\"\",c=i.twelveHourClock?\"%I\":\"%H\",h;a<o.minute?h=c+\":%M:%S\"+l:a<o.day?f<2*o.day?h=c+\":%M\"+l:h=\"%b %d \"+c+\":%M\"+l:a<o.month?h=\"%b %d\":u&&a<o.quarter||!u&&a<o.year?f<o.year?h=\"%b\":h=\"%b %Y\":u&&a<o.year?f<o.year?h=\"Q%q\":h=\"Q%q %Y\":h=\"%Y\";var p=r(n,h,i.monthNames,i.dayNames);return p})})})}var t={xaxis:{timezone:null,timeformat:null,twelveHourClock:!1,monthNames:null}},o={second:1e3,minute:6e4,hour:36e5,day:864e5,month:2592e6,quarter:7776e6,year:525949.2*60*1e3},u=[[1,\"second\"],[2,\"second\"],[5,\"second\"],[10,\"second\"],[30,\"second\"],[1,\"minute\"],[2,\"minute\"],[5,\"minute\"],[10,\"minute\"],[30,\"minute\"],[1,\"hour\"],[2,\"hour\"],[4,\"hour\"],[8,\"hour\"],[12,\"hour\"],[1,\"day\"],[2,\"day\"],[3,\"day\"],[.25,\"month\"],[.5,\"month\"],[1,\"month\"],[2,\"month\"]],a=u.concat([[3,\"month\"],[6,\"month\"],[1,\"year\"]]),f=u.concat([[1,\"quarter\"],[2,\"quarter\"],[1,\"year\"]]);e.plot.plugins.push({init:l,options:t,name:\"time\",version:\"1.0\"}),e.plot.formatDate=r})(jQuery);\n" +
                "    </script>" +
                "   </head>\n" +
                "   <body>\n" +
                "       <div class=\"container\">" +
                "       <h1>" + usageStatistics + "</h1>" +
                "       <table class=\"table table-striped table-hover\">";
    }

    private static String getFooter() {
        return "</body>\n</html>";
    }

    /**
     * Generates a subset of bootstrap css-styling rules
     *
     * @return the css code
     */
    private static String getCSS() {
        //Bootstrap 
        return "/* Bootstrap v2.3.2 *\n"
                + " * Copyright 2012 Twitter, Inc\n"
                + " * Licensed under the Apache License v2.0\n"
                + " * http://www.apache.org/licenses/LICENSE-2.0\n"
                + " *\n"
                + " * Designed and built with all the love in the world @twitter by @mdo and @fat.\n"
                + " */\n"
                + ".container { width: 650px; margin: 0 auto;}\n"
                + "html{font-size:100%;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;}\n"
                + "a:focus{outline:thin dotted #333;outline:5px auto -webkit-focus-ring-color;outline-offset:-2px;}\n"
                + "a:hover,a:active{outline:0;}\n"
                + "@media print{*{text-shadow:none !important;color:#000 !important;background:transparent !important;box-shadow:none !important;} a,a:visited{text-decoration:underline;} a[href]:after{content:\" (\" attr(href) \")\";} abbr[title]:after{content:\" (\" attr(title) \")\";} .ir a:after,a[href^=\"javascript:\"]:after,a[href^=\"#\"]:after{content:\"\";} pre,blockquote{border:1px solid #999;page-break-inside:avoid;} thead{display:table-header-group;} tr,img{page-break-inside:avoid;} img{max-width:100% !important;} @page {margin:0.5cm;}p,h2,h3{orphans:3;widows:3;} h2,h3{page-break-after:avoid;}}body{margin:0;font-family:\"Helvetica Neue\",Helvetica,Arial,sans-serif;font-size:14px;line-height:20px;color:#333333;background-color:#ffffff;}\n"
                + "a{color:#0088cc;text-decoration:none;}\n"
                + "a:hover,a:focus{color:#005580;text-decoration:underline;}\n"
                + "table{max-width:100%;background-color:transparent;border-collapse:collapse;border-spacing:0;}\n"
                + ".table{width:100%;margin-bottom:20px;}.table th,.table td{padding:8px;line-height:20px;text-align:left;vertical-align:top;border-top:1px solid #dddddd;}\n"
                + ".table th{font-weight:bold;}\n"
                + ".table thead th{vertical-align:bottom;}\n"
                + ".table caption+thead tr:first-child th,.table caption+thead tr:first-child td,.table colgroup+thead tr:first-child th,.table colgroup+thead tr:first-child td,.table thead:first-child tr:first-child th,.table thead:first-child tr:first-child td{border-top:0;}\n"
                + ".table tbody+tbody{border-top:2px solid #dddddd;}\n"
                + ".table .table{background-color:#ffffff;}\n"
                + ".table-striped tbody>tr:nth-child(odd)>td,.table-striped tbody>tr:nth-child(odd)>th{background-color:#f9f9f9;}\n"
                + ".table-hover tbody tr:hover>td,.table-hover tbody tr:hover>th{background-color:#f5f5f5;}\n"
                + ".table tbody tr.info>td{background-color:#d9edf7;}\n"
                + ".table-hover tbody tr.info:hover>td{background-color:#c4e3f3;}\n"
                + ".r {text-align: right !important;}";
    }

    private void readStatisticData() throws SQLException {
        PreparedStatement ps;
        ResultSet rs;
        LinkedHashMap<String, StatisticData> data = new LinkedHashMap<>();

        //         read data with country_location
        String sql = String.format(SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_LOCATION, SELECT_COUNTRY_COUNTRY_LOCATION, WHERE_COUNTRY_LOCATION);

        ps = conn.prepareStatement(sql);
        logger.debug(ps.toString());
        ps.execute();
        rs = ps.getResultSet();

        Date actualDate = null;
        while (rs.next()) {
            if (actualDate == null) {
                actualDate = rs.getDate(KEY_DAY_FROM);
            }
            if (actualDate.equals(rs.getDate(KEY_DAY_FROM))) {
                data.put(rs.getString(KEY_COUNTRY), new StatisticData(rs.getLong(KEY_COUNT_TESTS), rs.getLong(KEY_COUNT_CLIENTS), rs.getLong(KEY_COUNT_IPS), rs.getDate(KEY_DAY_FROM), rs.getString(KEY_COUNTRY)));
            } else {
//                we go to next date
                statisticData.put(rs.getDate(KEY_DAY_FROM), data);
//                reset data in
                actualDate = rs.getDate(KEY_DAY_FROM);
                data = new LinkedHashMap<>();
            }
        }

//        read data with country_asn
        sql = String.format(SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_ASN, SELECT_COUNTRY_COUNTRY_ASN, WHERE_COUNTRY_ASN);
        countStatisticData(sql);

//        read data with network_sim_country
        sql = String.format(SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_COUNTRY_GEOIP, SELECT_COUNTRY_COUNTRY_GEOIP, WHERE_COUNTRY_GEOIP);
        countStatisticData(sql);

        sql = String.format(SELECT_COUNTRY_WEEKLY_STATISTICS_FOR_LAST_365_DAYS_GROUP_NETWORK_GROUP_NAME, SELECT_COUNTRY_NETWORK_GROUP_NAME, WHERE_COUNTRY_NETWORK_GROUP_NAME);
        countStatisticData(sql);
    }

    private void countStatisticData(String sql) throws SQLException {
        PreparedStatement ps;
        ResultSet rs;
        LinkedHashMap<String, StatisticData> data = new LinkedHashMap<>();
        LinkedHashMap<String, StatisticData> countryData = new LinkedHashMap<>();
        StatisticData sd = null;
        Date queryDate = null;
        String queryCountry = null;

        ps = conn.prepareStatement(sql);
        logger.debug("SQL: " + ps.toString() );
        rs = ps.executeQuery();

        while (rs.next()) {
            data = null;

            queryDate = rs.getDate(KEY_DAY_FROM);
            queryCountry = rs.getString(KEY_COUNTRY);
            if (statisticData.containsKey(queryDate)) {
//                Date exist in linkedhashmap
                countryData = statisticData.get(queryDate);
                if (countryData.containsKey(queryCountry)) {
//                    Country exist in linkedhashmap and we can update it
                    sd = countryData.get(queryCountry);
//                    update data
                    sd.addCountClients(rs.getLong(KEY_COUNT_CLIENTS));
                    sd.addCountIPs(rs.getLong(KEY_COUNT_IPS));
                    sd.addCountTests(rs.getLong(KEY_COUNT_TESTS));

                    countryData.put(queryCountry, sd);
                } else {
//                    country doesn't exist in linkedhashmap
                    sd = new StatisticData(rs.getLong(KEY_COUNT_TESTS), rs.getLong(KEY_COUNT_CLIENTS), rs.getLong(KEY_COUNT_IPS), rs.getDate(KEY_DAY_FROM), queryCountry);
                    countryData.put(queryCountry, sd);
                }
                statisticData.put(queryDate, countryData);
            } else {
//                date doesn't exist in linkedhashmap
                data = new LinkedHashMap<>();
                data.put(queryCountry, new StatisticData(rs.getLong(KEY_COUNT_TESTS), rs.getLong(KEY_COUNT_CLIENTS), rs.getLong(KEY_COUNT_IPS), rs.getDate(KEY_DAY_FROM), queryCountry));
                statisticData.put(queryDate, data);
            }
        }

    }
}
