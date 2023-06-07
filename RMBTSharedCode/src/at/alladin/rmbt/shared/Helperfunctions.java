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
package at.alladin.rmbt.shared;

import at.alladin.rmbt.util.ExpirableConcurrentHashMap;
import com.google.common.net.InetAddresses;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Helperfunctions {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(Helperfunctions.class);

    private static final long EXPIRE_AFTER_MMS = 86400000L;
    private static final ExpirableConcurrentHashMap asnNumberMap = new ExpirableConcurrentHashMap<String, Long>(EXPIRE_AFTER_MMS);
    private static final ExpirableConcurrentHashMap asnNameMap = new ExpirableConcurrentHashMap<Long, String>(EXPIRE_AFTER_MMS);
    private static final ExpirableConcurrentHashMap asnCountryMap = new ExpirableConcurrentHashMap<Long, String>(EXPIRE_AFTER_MMS);

    public static String calculateHMAC(final String secret, final String data) {
        try {
            final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            final byte[] rawHmac = mac.doFinal(data.getBytes());
            final String result = new String(Base64.encodeBytes(rawHmac));
            return result;
        } catch (final GeneralSecurityException e) {
            logger.error("Unexpected error while creating hash: " + e.getMessage());
            return "";
        }
    }

    /**
     * @return
     */
    public static String getTimezoneId() {
        return TimeZone.getDefault().getID();
    }

    public static TimeZone getTimeZone(final String id) {
        if (id == null)
            return TimeZone.getDefault();
        else
            return TimeZone.getTimeZone(id);
    }

    public static Calendar getTimeWithTimeZone(final String timezoneId) {

        final TimeZone timeZone = TimeZone.getTimeZone(timezoneId);

        final Calendar timeWithZone = Calendar.getInstance(timeZone);

        return timeWithZone;
    }

    public static SimpleDateFormat getDateFormat(final String lang) {
        SimpleDateFormat format;

        if (lang.equals("de"))
            format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        else
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format;
    }

    public static String geoToString(final Double geoLat, final Double geoLong) {
        final String DEGREE = "\u00b0";
        final char charDEGREE = '\u00b0';

        if (geoLat == null || geoLong == null)
            return null;

        int latd, lond; // latitude degrees and minutes, longitude degrees and
        // minutes
        double latm, lonm; // latitude and longitude seconds.

        // decimal degrees to degrees minutes seconds

        double temp;
        // latitude
        temp = java.lang.Math.abs(geoLat);
        latd = (int) temp;
        latm = (temp - latd) * 60.0;
        if (geoLat < 0)
            latd = -latd;

        // longitude
        temp = java.lang.Math.abs(geoLong);
        lond = (int) temp;
        lonm = (temp - lond) * 60.0;
        if (geoLong < 0)
            lond = -lond;

        final String dirLat;
        if (geoLat >= 0)
            dirLat = "N";
        else
            dirLat = "S";
        final String dirLon;
        if (geoLong >= 0)
            dirLon = "E";
        else
            dirLon = "W";

        //return String.format("%s %02dKRUZOK%02.3f'  %s %02dKRUZOK%02.3f'", dirLat, latd, latm, dirLon, lond, lonm);
        try {
            return String.format("%s %02d%s%02.3f'  %s %02d%s%02.3f'", dirLat, latd, DEGREE, latm, dirLon, lond, DEGREE, lonm);
        } catch (Exception e) {
            logger.error("Formatting location error: " + e.getMessage());
            return " ";
        }
    }

    // Just quick and dirty hack for Slovak RU - ticket SDRU-127
    public static String getNetworkTypeNameTranslated(final int type, final String lang, ResourceBundle resourceBundle) {
        //System.out.println("Translating network type into language:" + lang);
        if (lang.equalsIgnoreCase("SK")) {
            return getNetworkTypeName_SK(type, resourceBundle);
        }
        return getNetworkTypeName(type);
    }

    public static String getNetworkTypeName(final int type) {

        // TODO: read from DB
        switch (type) {
            case 1:
                return "2G (GSM)";
            case 2:
                return "2G (EDGE)";
            case 3:
                return "3G (UMTS)";
            case 4:
                return "2G (CDMA)";
            case 5:
                return "2G (EVDO_0)";
            case 6:
                return "2G (EVDO_A)";
            case 7:
                return "2G (1xRTT)";
            case 8:
                return "3G (HSDPA)";
            case 9:
                return "3G (HSUPA)";
            case 10:
                return "3G (HSPA)";
            case 11:
                return "2G (IDEN)";
            case 12:
                return "2G (EVDO_B)";
            case 13:
                return "4G (LTE)";
            case 14:
                return "2G (EHRPD)";
            case 15:
                return "3G (HSPA+)";
            case 16:
                return "2G (GSM)";
            case 17:
                return "3G (TD_SCDMA)";
            case 18:
                return "UNKNOWN (IWLAN)";
            case 19:
                return "4G (LTE CA)";
            case 20:
                return "5G (NR)";
            case 40:
                return "4G (5G Signalling)";
            case 41:
                return "5G (NRNSA)";
            case 97:
                return "CLI";
            case 98:
                return "BROWSER";
            case 99:
                return "WLAN";
            case 101:
                return "2G/3G";
            case 102:
                return "3G/4G";
            case 103:
                return "2G/4G";
            case 104:
                return "2G/3G/4G";
            case 105:
                return "MOBILE";
            case 106:
                return "Ethernet";
            case 107:
                return "Bluetooth";
            default:
                return "UNKNOWN";
        }
    }

    // this methods converts network name into Slovak language.
    // Just quick and dirty hack for Slovak RU - ticket SDRU-127
    public static String getNetworkTypeName_SK(final int type, ResourceBundle resourceBundle) {

        // TODO: read from DB
        switch (type) {
            case 1:
                return "2G (GSM)";
            case 2:
                return "2G (EDGE)";
            case 3:
                return "3G (UMTS)";
            case 4:
                return "2G (CDMA)";
            case 5:
                return "2G (EVDO_0)";
            case 6:
                return "2G (EVDO_A)";
            case 7:
                return "2G (1xRTT)";
            case 8:
                return "3G (HSDPA)";
            case 9:
                return "3G (HSUPA)";
            case 10:
                return "3G (HSPA)";
            case 11:
                return "2G (IDEN)";
            case 12:
                return "2G (EVDO_B)";
            case 13:
                return "4G (LTE)";
            case 14:
                return "2G (EHRPD)";
            case 15:
                return "3G (HSPA+)";
            case 16:
                return "2G (GSM)";
            case 17:
                return "3G (TD_SCDMA)";
            case 18:
                return "UNKNOWN (IWLAN)";
            case 19:
                return "4G (LTE CA)";
            case 20:
                return "5G (NR)";
            case 40:
                return "4G (5G Signalling)";
            case 41:
                return "5G (NRNSA)";
            case 97:
                return "CLI";
            case 98:
                return resourceBundle.getString("MAP_BROWSER");//"Prehliadač";
            case 99:
                return "WLAN";
            case 101:
                return "2G/3G";
            case 102:
                return "3G/4G";
            case 103:
                return "2G/4G";
            case 104:
                return "2G/3G/4G";
            case 105:
                return "Mobil";
            case 106:
                return "Ethernet";
            case 107:
                return "Bluetooth";
            default:
                return resourceBundle.getString("RESULT_UNKNOWN");//"Neznámy";
        }
    }

    // this methods converts network group name into different languages.
    // Just quick and dirty hack for Slovak RU - ticket SDRU-127
    public static String getNetworkTypeGroupName(final String type, final String lang, ResourceBundle resourceBundle) {
        if (lang.equalsIgnoreCase("SK")) {
            if (type.equalsIgnoreCase("LAN")) return resourceBundle.getString("MAP_BROWSER");//"Prehliadač";
            if (type.equalsIgnoreCase("MOBILE")) return "Mobil";

        }
        return type;
    }


    public static String getRoamingType(final ResourceBundle labels, final int roamingType) {
        final String roamingValue;
        switch (roamingType) {
            case 0:
                roamingValue = labels.getString("value_roaming_none");
                break;
            case 1:
                roamingValue = labels.getString("value_roaming_national");
                break;
            case 2:
                roamingValue = labels.getString("value_roaming_international");
                break;
            default:
                roamingValue = "?";
                break;
        }
        return roamingValue;
    }

    public static boolean isIPLocal(final InetAddress adr) {
        return adr.isLinkLocalAddress() || adr.isLoopbackAddress() || adr.isSiteLocalAddress();
    }

    /*
       public static String filterIp(InetAddress inetAddress)
       { // obsoleted by removal of old client_local_ip column
           try
           {
               final String ipVersion;
               if (inetAddress instanceof Inet4Address)
                   ipVersion = "ipv4";
               else if (inetAddress instanceof Inet6Address)
                   ipVersion = "ipv6";
               else
                   ipVersion = "ipv?";

               if (inetAddress.isAnyLocalAddress())
                   return "wildcard_" + ipVersion;
               if (inetAddress.isSiteLocalAddress())
                   return "site_local_" + ipVersion;
               if (inetAddress.isLinkLocalAddress())
                   return "link_local_" + ipVersion;
               if (inetAddress.isLoopbackAddress())
                   return "loopback_" + ipVersion;
               return InetAddresses.toAddrString(inetAddress);
           }
           catch (final IllegalArgumentException e)
           {
               return "illegal_ip";
           }
       }

      */
    public static String IpType(InetAddress inetAddress) {
        try {
            final String ipVersion;
            if (inetAddress instanceof Inet4Address)
                ipVersion = "ipv4";
            else if (inetAddress instanceof Inet6Address)
                ipVersion = "ipv6";
            else
                ipVersion = "ipv?";

            if (inetAddress.isAnyLocalAddress())
                return "wildcard_" + ipVersion;
            if (inetAddress.isSiteLocalAddress())
                return "site_local_" + ipVersion;
            if (inetAddress.isLinkLocalAddress())
                return "link_local_" + ipVersion;
            if (inetAddress.isLoopbackAddress())
                return "loopback_" + ipVersion;
            return "public_" + ipVersion;

        } catch (final IllegalArgumentException e) {
            logger.error(e.getMessage());
            return "illegal_ip";
        }
    }


    public static String anonymizeIp(final InetAddress inetAddress) {
        try {
            final byte[] address = inetAddress.getAddress();
            address[address.length - 1] = 0;
            if (address.length > 4) // ipv6
            {
                for (int i = 6; i < address.length; i++)
                    address[i] = 0;
            }

            String result = InetAddresses.toAddrString(InetAddress.getByAddress(address));
            if (address.length == 4)
                result = result.replaceFirst(".0$", "");
            return result;
        } catch (final Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static String getNatType(final InetAddress localAdr, final InetAddress publicAdr) {
        try {
            final String ipVersion;
            if (publicAdr instanceof Inet4Address)
                ipVersion = "ipv4";
            else if (publicAdr instanceof Inet6Address)
                ipVersion = "ipv6";
            else
                ipVersion = "ipv?";

            if (localAdr.equals(publicAdr))
                return "no_nat_" + ipVersion;
            else {
                final String localType = isIPLocal(localAdr) ? "local" : "public";
                final String publicType = isIPLocal(publicAdr) ? "local" : "public";
                return String.format("nat_%s_to_%s_%s", localType, publicType, ipVersion);
            }
        } catch (final IllegalArgumentException e) {
            logger.error(e.getMessage());
            return "illegal_ip";
        }
    }

    public static String reverseDNSLookup(final InetAddress adr) {
        try {
            final Name name = ReverseMap.fromAddress(adr);

            final Lookup lookup = new Lookup(name, Type.PTR);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            final Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL)
                for (final Record record : records)
                    if (record instanceof PTRRecord) {
                        final PTRRecord ptr = (PTRRecord) record;
                        return ptr.getTarget().toString();
                    }
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public static Name getReverseIPName(final InetAddress adr, final Name postfix) {
        final byte[] addr = adr.getAddress();
        final StringBuilder sb = new StringBuilder();
        if (addr.length == 4)
            for (int i = addr.length - 1; i >= 0; i--) {
                sb.append(addr[i] & 0xFF);
                if (i > 0)
                    sb.append(".");
            }
        else {
            final int[] nibbles = new int[2];
            for (int i = addr.length - 1; i >= 0; i--) {
                nibbles[0] = (addr[i] & 0xFF) >> 4;
                nibbles[1] = addr[i] & 0xFF & 0xF;
                for (int j = nibbles.length - 1; j >= 0; j--) {
                    sb.append(Integer.toHexString(nibbles[j]));
                    if (i > 0 || j > 0)
                        sb.append(".");
                }
            }
        }
        try {
            return Name.fromString(sb.toString(), postfix);
        } catch (final TextParseException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException("name cannot be invalid");
        }
    }

    public static Long getASN(final InetAddress adr) {
        try {
            Long toReturn = (Long) asnNumberMap.get(adr.getHostAddress());

            // check hashmap first
            if( toReturn == null) {

                final Name postfix;
                if (adr instanceof Inet6Address)
                    postfix = Name.fromConstantString("origin6.asn.cymru.com");
                else
                    postfix = Name.fromConstantString("origin.asn.cymru.com");

                final Name name = getReverseIPName(adr, postfix);
                logger.debug("lookup: " + name);

                final Lookup lookup = new Lookup(name, Type.TXT);
                lookup.setResolver(new SimpleResolver());
                lookup.setCache(null);
                final Record[] records = lookup.run();
                if (lookup.getResult() == Lookup.SUCCESSFUL)
                    for (final Record record : records)
                        if (record instanceof TXTRecord) {
                            final TXTRecord txt = (TXTRecord) record;
                            @SuppressWarnings("unchecked") final List<String> strings = txt.getStrings();
                            if (strings != null && !strings.isEmpty()) {
                                final String result = strings.get(0);
                                final String[] parts = result.split(" ?\\| ?");
                                if (parts != null && parts.length >= 1) {
                                    toReturn = new Long(parts[0]);
                                    asnNumberMap.put( adr.getHostAddress(), toReturn);
                                    logger.debug("inserted new address: " + adr.getHostAddress() + ", asn: " + toReturn);
                                }
                            }
                        }
            } else logger.debug("found cached asn number: " + toReturn);

            return toReturn;
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        logger.error("NOT FOUND asn number for: " + adr.getHostAddress() + " !!!");
        return null;
    }

    public static String getASName(final long asn) {
        try {
            String toReturn = (String) asnNameMap.get(asn);

            // check hashmap first
            if( toReturn == null) {
                final Name postfix = Name.fromConstantString("asn.cymru.com.");
                final Name name = new Name(String.format("AS%d", asn), postfix);
                logger.debug("lookup: " + name);

                final Lookup lookup = new Lookup(name, Type.TXT);
                lookup.setResolver(new SimpleResolver());
                lookup.setCache(null);
                final Record[] records = lookup.run();
                if (lookup.getResult() == Lookup.SUCCESSFUL)
                    for (final Record record : records)
                        if (record instanceof TXTRecord) {
                            final TXTRecord txt = (TXTRecord) record;
                            @SuppressWarnings("unchecked") final List<String> strings = txt.getStrings();
                            if (strings != null && !strings.isEmpty()) {
                                //System.out.println(strings);

                                final String result = strings.get(0);
                                final String[] parts = result.split(" ?\\| ?");
                                if (parts != null && parts.length >= 1) {
                                    toReturn = parts[4];
                                    asnNameMap.put(asn, toReturn);
                                    logger.debug("inserted new asn: " + asn + ", name: " + toReturn);
                                }
                            }
                        }
            } else logger.debug("found cached asn name for asn: " + asn);

            return toReturn;
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        logger.error("NOT FOUND name for asn: " + asn + " !!!");
        return null;
    }

    public static String getAScountry(final long asn) {
        try {
            String toReturn = (String) asnCountryMap.get(asn);

            // check hashmap first
            if( toReturn == null) {

                final Name postfix = Name.fromConstantString("asn.cymru.com.");
                final Name name = new Name(String.format("AS%d", asn), postfix);
                logger.debug("lookup: " + name);

                final Lookup lookup = new Lookup(name, Type.TXT);
                lookup.setResolver(new SimpleResolver());
                lookup.setCache(null);
                final Record[] records = lookup.run();
                if (lookup.getResult() == Lookup.SUCCESSFUL)
                    for (final Record record : records)
                        if (record instanceof TXTRecord) {
                            final TXTRecord txt = (TXTRecord) record;
                            @SuppressWarnings("unchecked") final List<String> strings = txt.getStrings();
                            if (strings != null && !strings.isEmpty()) {
                                final String result = strings.get(0);
                                final String[] parts = result.split(" ?\\| ?");
                                if (parts != null && parts.length >= 1) {
                                    toReturn = parts[1];
                                    asnCountryMap.put( asn, toReturn);
                                    logger.debug("inserted new asn: " + asn + ", country: " + toReturn);
                                }
                            }
                        }
            } else logger.debug("found cached asn country for asn: " + asn);

            return toReturn;
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        logger.error("NOT FOUND country for asn: " + asn + " !!!");
        return null;
    }

    /**
     * @param <T>
     * @param array
     * @return
     */
    public static <T extends Object> String join(String glue, T[] array) {
        StringBuilder sb = new StringBuilder("");

        int len = array.length;

        if (len < 1) {
            return null;
        }

        for (int i = 0; i < (len - 1); i++) {
            sb.append(String.valueOf(array[i]));
            sb.append(glue);
        }

        sb.append(String.valueOf(array[len - 1]));

        return sb.toString();
    }

    /**
     * @param json
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String json2htmlWithLinks(JSONObject json) {
        StringBuilder result = new StringBuilder();

        Iterator<String> jsonIterator = json.keys();

        try {
            while (jsonIterator.hasNext()) {
                String key = jsonIterator.next();

                if (json.opt(key) instanceof JSONObject) {
                    result.append("\"" + key + "\" => \"" + json2hstore(json.optJSONObject(key), null) + "\"");
                } else {
                    String keyValue = json.getString(key).replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'");
                    if ("on_success".equals(key) || "on_failure".equals(key)) {
                        String link = "<a href=\"#" + keyValue.replaceAll("[\\-\\+\\.\\^:,]", "_") + "\">" + keyValue + "</a>";
                        result.append("\"" + key + "\" => \"" + link + "\"");
                    } else {
                        result.append("\"" + key + "\" => \"" + keyValue + "\"");
                    }
                }

                if (jsonIterator.hasNext()) {
                    result.append(", ");
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
            return null;
        }

        return result.toString();
    }

    /**
     * @param json
     * @param excludeKeys
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String json2hstore(JSONObject json, Set<String> excludeKeys) {
        StringBuilder result = new StringBuilder();

        Iterator<String> jsonIterator = json.keys();

        try {
            boolean isFirst = true;
            while (jsonIterator.hasNext()) {
                String key = jsonIterator.next();

                if (excludeKeys == null || !excludeKeys.contains(key)) {
                    if (!isFirst) {
                        if (json.opt(key) instanceof JSONObject) {
                            result.append(", \"" + key + "\" => \"" + json2hstore(json.optJSONObject(key), excludeKeys) + "\"");
                        } else {
                            Object data = json.get(key);
                            if (data != null) {
                                if (data instanceof String) {
                                    data = "\"" + ((String) data).replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'") + "\"";
                                } else if (data instanceof JSONArray) {
                                    data = "\"" + ((JSONArray) data).toString().replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'") + "\"";
                                }
                            }
                            result.append(", \"" + key + "\" => " + data);
                            //result.append(", \"" + key + "\" => \"" + json.getString(key).replaceAll("\"","\\\\\"").replaceAll("'","\\\\'") + "\"");
                        }
                    } else {
                        isFirst = false;
                        if (json.opt(key) instanceof JSONObject) {
                            result.append("\"" + key + "\" => \"" + json2hstore(json.optJSONObject(key), excludeKeys) + "\"");
                        } else {
                            Object data = json.get(key);
                            if (data != null) {
                                if (data instanceof String) {
                                    data = "\"" + ((String) data).replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'") + "\"";
                                } else if (data instanceof JSONArray) {
                                    data = "\"" + ((JSONArray) data).toString().replaceAll("\"", "\\\\\"").replaceAll("'", "\\\\'") + "\"";
                                }
                            }
                            result.append("\"" + key + "\" => " + data);
                            //result.append("\"" + key + "\" => \"" + json.getString(key).replaceAll("\"","\\\\\"").replaceAll("'","\\\\'") + "\"");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            logger.error(e.getMessage());
            return null;
        }

        return result.toString();
    }

    public static boolean isEmailValid(String email){
        Pattern pattern;
        Matcher matcher;

        String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        pattern = Pattern.compile(EMAIL_PATTERN);

        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
