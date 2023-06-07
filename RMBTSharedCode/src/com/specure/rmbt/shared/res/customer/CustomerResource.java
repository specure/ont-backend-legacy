package com.specure.rmbt.shared.res.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.InputStream;

public final class CustomerResource {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(CustomerResource.class);

    // singleton, class instance
    private static CustomerResource instance = null;

    public synchronized static CustomerResource getInstance() {
        if (instance == null) {
            instance = new CustomerResource();
        }
        return instance;
    }

    // customer's name
    public String getCustomer() {
        String customer = "specure";
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            customer = (String) environmentContext.lookup("customer");
            logger.debug("Customer: " + customer);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return customer;
    }

    public Boolean showPointsOnMap() {
        Boolean showPointsOnMap = true;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showPointsOnMap = (Boolean) environmentContext.lookup("showPointsOnMap");
            logger.debug("ShowPointsOnMap: " + showPointsOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showPointsOnMap;
    }

    public Boolean showNewProviderInMapFilter() {
        Boolean showNewProviderInMapFilter = true;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showNewProviderInMapFilter = (Boolean) environmentContext.lookup("showNewProviderInMapFilter");
            logger.debug("ShowNewProviderInMapFilter: " + showNewProviderInMapFilter);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showNewProviderInMapFilter;
    }

    public Boolean showCountriesInMapFilter() {
        Boolean showCountriesInMapFilter = true;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showCountriesInMapFilter = (Boolean) environmentContext.lookup("showCountriesInMapFilter");
            logger.debug("ShowCountriesInMapFilter: " + showCountriesInMapFilter);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showCountriesInMapFilter;
    }

    public Boolean showDeviceInfoOnMap() {
        Boolean showDeviceInfoOnMap = true;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showDeviceInfoOnMap = (Boolean) environmentContext.lookup("showDeviceInfoOnMap");
            logger.debug("ShowDeviceInfoOnMap: " + showDeviceInfoOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showDeviceInfoOnMap;
    }

    public Boolean showRegionsOnMap() {
        Boolean showRegionsOnMap = false;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showRegionsOnMap = (Boolean) environmentContext.lookup("showRegionsOnMap");
            logger.debug("ShowRegionsOnMap: " + showRegionsOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showRegionsOnMap;
    }

    public Boolean showMunicipalitiesOnMap() {
        Boolean showMunicipalitiesOnMap = false;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showMunicipalitiesOnMap = (Boolean) environmentContext.lookup("showMunicipalitiesOnMap");
            logger.debug("ShowMunicipalitiesOnMap: " + showMunicipalitiesOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showMunicipalitiesOnMap;
    }

    public Boolean showSettlementsOnMap() {
        Boolean showSettlementsOnMap = false;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showSettlementsOnMap = (Boolean) environmentContext.lookup("showSettlementsOnMap");
            logger.debug("ShowSettlementsOnMap: " + showSettlementsOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showSettlementsOnMap;
    }

    public Boolean showWhiteSpotsOnMap() {
        Boolean showWhiteSpotsOnMap = false;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            showWhiteSpotsOnMap = (Boolean) environmentContext.lookup("showWhiteSpotsOnMap");
            logger.debug("ShowWhiteSpotsOnMap: " + showWhiteSpotsOnMap);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return showWhiteSpotsOnMap;
    }

    public Boolean showZeroMeasurements() {
        return getFromContextByName("showZeroMeasurementsOnMap", true);
    }

    public Boolean checkMeasurementServerVersion() {
        Boolean checkMeasurementServerVersion = false;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            checkMeasurementServerVersion = (Boolean) environmentContext.lookup("checkMeasurementServerVersion");
            logger.debug("checkMeasurementServerVersion: " + checkMeasurementServerVersion);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return checkMeasurementServerVersion;
    }

    private Boolean getFromContextByName(String name, Boolean defaultValue) {
        Boolean result = defaultValue;
        try {
            Context environmentContext = (Context) new InitialContext().lookup("java:/comp/env");
            result = (Boolean) environmentContext.lookup(name);
            logger.debug(name +": " + result);
        } catch (NamingException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    public InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }
}
