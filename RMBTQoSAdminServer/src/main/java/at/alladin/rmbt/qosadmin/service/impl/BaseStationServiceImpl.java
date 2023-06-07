package at.alladin.rmbt.qosadmin.service.impl;

import at.alladin.rmbt.qosadmin.config.db.RmbtTx;
import at.alladin.rmbt.qosadmin.model.BaseStation;
import at.alladin.rmbt.qosadmin.repository.BaseStationRepository;
import at.alladin.rmbt.qosadmin.service.BaseStationService;
import at.alladin.rmbt.qosadmin.util.IllegalInputException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@RmbtTx
@Service
public class BaseStationServiceImpl implements BaseStationService {
    @Autowired
    BaseStationRepository baseStationRepository;

    public long importFile(final InputStream inputStream, int sheetIndex) throws IllegalInputException {
        return storeAndReplace(readBaseStations(inputStream, sheetIndex));
    }

    public long storeAndReplace(final List<BaseStation> baseStationList) {
        baseStationRepository.deleteAll();
        return baseStationRepository.save(baseStationList).size();
    }

    public List<BaseStation> readBaseStations(final InputStream inputStream, int sheetIndex) throws IllegalInputException {
        try {
            final Workbook wb = WorkbookFactory.create(inputStream);
            final Sheet sheet = wb.getSheetAt(sheetIndex);
            final Row headerRow = sheet.getRow(0);

            final short minColIx = headerRow.getFirstCellNum();
            final short maxColIx = headerRow.getLastCellNum();
            final String[] columns = new String[maxColIx + 1];
            for (short colIx = minColIx; colIx < maxColIx; colIx++) {
                final Cell cell = headerRow.getCell(colIx);
                if (cell == null)
                    continue;
                columns[colIx] = cell.getStringCellValue();
                if (columns[colIx] != null) {
                    if (columns[colIx].isEmpty())
                        columns[colIx] = null;
                    else
                        columns[colIx] = columns[colIx].toLowerCase(Locale.US);
                }
            }

            final List<BaseStation> result = new ArrayList<BaseStation>();
            final int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                final Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                final BaseStation bs = new BaseStation();
                boolean emptyRow = true;
                for (int j = 0; j < columns.length; j++) {
                    if (columns[j] == null)
                        continue;
                    final Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;

                    emptyRow = false;
                    switch (columns[j]) {
                        case "technology":
                            bs.setTechnology(cell.getStringCellValue());
                            break;

                        case "location_name":
                            bs.setLocationName(cell.getStringCellValue());
                            break;

                        case "longitude":
                            bs.setLongitude(cell.getNumericCellValue());
                            break;

                        case "latitude":
                            bs.setLatitude(cell.getNumericCellValue());
                            break;

                        case "mnc":
                            bs.setMnc((int) cell.getNumericCellValue());
                            break;

                        case "ci":
                            bs.setCi((int) cell.getNumericCellValue());
                            break;

                        case "lac":
                            bs.setLac((int) cell.getNumericCellValue());
                            break;

                        case "enb":
                            bs.setEnb((int) cell.getNumericCellValue());
                            break;

                        case "physical_cell_id":
                            bs.setPhysicalCellId((int) cell.getNumericCellValue());
                            break;

                        case "eci":
                            bs.setEci((int) cell.getNumericCellValue());
                            break;

                        case "tac":
                            bs.setTac((int) cell.getNumericCellValue());
                            break;

                        case "rf_band":
                            bs.setRfBand(cell.getStringCellValue());
                            break;
                    }
                }
                if (!emptyRow)
                    result.add(bs);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalInputException("error while reading base stations: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        final BaseStationServiceImpl b = new BaseStationServiceImpl();
        final List<BaseStation> baseStations = b.readBaseStations(new FileInputStream(args[0]), 0);
        for (BaseStation bs : baseStations)
            System.out.println(bs);
    }

}
