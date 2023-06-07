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
package at.alladin.rmbt;

import com.maxmind.geoip.Country;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;

public class GeoIPHelper {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(GeoIPHelper.class);

    private static GeoIPHelper instance = null;

    private static final String GEO_CITY_IPv4_PATH = "GeoLiteCityIPv4.dat";
    private static final String GEO_CITY_IPv6_PATH = "GeoLiteCityIPv6.dat";
    private static final String GEO_COUNTRY_IPv4_PATH = "GeoIPv4.dat";
    private static final String GEO_COUNTRY_IPv6_PATH = "GeoIPv6.dat";

    private LookupService lookupServiceGeoLiteCityV4;
    private LookupService lookupServiceGeoLiteCityV6;
    private LookupService lookupServiceGeoIPV4;
    private LookupService lookupServiceGeoIPV6;

    private File fileGeoLiteCityIPv4;
    private File fileGeoLiteCityIPv6;
    private File fileGeoIPv4;
    private File fileGeoIPv6;

    public static synchronized GeoIPHelper getInstance() {
        if (instance == null) {
            instance = new GeoIPHelper();
        }
        return instance;
    }

    public GeoIPHelper() {

        // check for GeoLiteCityIPv4.dat
        InputStream isGeoLiteCityIPv4 = GeoIPHelper.class.getResourceAsStream(GEO_CITY_IPv4_PATH);
        try {
            fileGeoLiteCityIPv4 = stream2file(isGeoLiteCityIPv4, GEO_CITY_IPv4_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (fileGeoLiteCityIPv4 == null || fileGeoLiteCityIPv4.exists() == false || fileGeoLiteCityIPv4.isFile() == false) {
            logger.error("GeoLiteCityIPv4.dat not found !!!");
        }

        // check for GeoLiteCityIPv6.dat
        InputStream isGeoLiteCityIPv6 = GeoIPHelper.class.getResourceAsStream(GEO_CITY_IPv6_PATH);
        try {
            fileGeoLiteCityIPv6 = stream2file(isGeoLiteCityIPv6, GEO_CITY_IPv6_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (fileGeoLiteCityIPv6 == null || fileGeoLiteCityIPv6.exists() == false || fileGeoLiteCityIPv6.isFile() == false) {
            logger.error("GeoLiteCityIPv6.dat not found !!!");
        }

        // check for GeoIPv4.dat
        InputStream isGeoIPv4 = GeoIPHelper.class.getResourceAsStream(GEO_COUNTRY_IPv4_PATH);
        try {
            fileGeoIPv4 = stream2file(isGeoIPv4, GEO_COUNTRY_IPv4_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (fileGeoIPv4 == null || fileGeoIPv4.exists() == false || fileGeoIPv4.isFile() == false) {
            logger.error("GeoIPv4.dat not found !!!");
        }

        // check for GeoIPv6.dat
        InputStream isGeoIPv6 = GeoIPHelper.class.getResourceAsStream(GEO_COUNTRY_IPv6_PATH);
        try {
            fileGeoIPv6 = stream2file(isGeoIPv6, GEO_COUNTRY_IPv6_PATH);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        if (fileGeoIPv6 == null || fileGeoIPv6.exists() == false || fileGeoIPv6.isFile() == false) {
            logger.error("GeoIPv6.dat not found !!!");
        }


        try {

            lookupServiceGeoLiteCityV4 = new LookupService(fileGeoLiteCityIPv4, LookupService.GEOIP_MEMORY_CACHE);
            if (lookupServiceGeoLiteCityV4 == null) {
                logger.error("Could not initialize lookupServiceGeoLiteCityV4 !!!");
            }

            lookupServiceGeoLiteCityV6 = new LookupService(fileGeoLiteCityIPv6, LookupService.GEOIP_MEMORY_CACHE);
            if (lookupServiceGeoLiteCityV6 == null) {
                logger.error("Could not initialize lookupServiceGeoLiteCityV6 !!!");
            }

            lookupServiceGeoIPV4 = new LookupService(fileGeoIPv4, LookupService.GEOIP_MEMORY_CACHE);
            if (lookupServiceGeoIPV4 == null) {
                logger.error("Could not initialize lookupServiceGeoIPV4 !!!");
            }
            lookupServiceGeoIPV6 = new LookupService(fileGeoIPv6, LookupService.GEOIP_MEMORY_CACHE);
            if (lookupServiceGeoIPV6 == null) {
                logger.error("Could not initialize lookupServiceGeoIPV6 !!!");
            }

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

    }

    public String lookupCountry(final InetAddress inetAddress) {
        try {
            Country country = null;
            if (inetAddress instanceof Inet6Address) {
                // ipv6
                country = lookupServiceGeoIPV6.getCountry(inetAddress);
            } else {
                // ipv4
                country = lookupServiceGeoIPV4.getCountry(inetAddress);
            }

            logger.debug("InetAddress: " + inetAddress + ", Country Code: " + country != null ? country.getCode() : null);
            return country != null ? country.getCode() : null;

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    public float getLatitudeFromIP(InetAddress inetAddress) {//String ipAdress) {
        try {
            Location location;
            if (inetAddress instanceof Inet6Address) {
                // ipv6
                location = lookupServiceGeoLiteCityV6.getLocation(inetAddress);
            } else {
                // ipv4
                location = lookupServiceGeoLiteCityV4.getLocation(inetAddress);
            }

            logger.debug("InetAddress: " + inetAddress + ", Latitude: " + (location != null ? location.latitude : Float.MAX_VALUE));
            return location != null ? location.latitude : Float.MAX_VALUE;

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return Float.MAX_VALUE;
        }
    }

    public float getLongitudeFromIP(InetAddress inetAddress) {
        try {
            Location location;
            if (inetAddress instanceof Inet6Address) {
                // ipv6
                location = lookupServiceGeoLiteCityV6.getLocation(inetAddress);
            } else {
                // ipv4
                location = lookupServiceGeoLiteCityV4.getLocation(inetAddress);
            }

            logger.debug("InetAddress: " + inetAddress + ", Longitude: " + (location != null ? location.longitude : Float.MAX_VALUE));
            return location != null ? location.longitude : Float.MAX_VALUE;

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return Float.MAX_VALUE;
        }
    }

    private File stream2file(InputStream in, String filename) throws IOException {
        File tempFile = File.createTempFile(filename, ".tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

}
