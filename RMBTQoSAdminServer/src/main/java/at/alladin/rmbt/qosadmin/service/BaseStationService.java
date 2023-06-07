package at.alladin.rmbt.qosadmin.service;

import at.alladin.rmbt.qosadmin.model.BaseStation;
import at.alladin.rmbt.qosadmin.util.IllegalInputException;

import java.io.InputStream;
import java.util.List;

public interface BaseStationService {

    public long importFile(final InputStream inputStream, int sheetIndex) throws IllegalInputException;

    public long storeAndReplace(final List<BaseStation> baseStationList);

    public List<BaseStation> readBaseStations(final InputStream inputStream, int sheetIndex) throws IllegalInputException;
}
