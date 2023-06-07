package at.alladin.rmbt.shared.db;

import at.alladin.rmbt.shared.SQLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public final class CellInfo {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(CellInfo.class);

    // SQL insert
    private static final String SQL_INSERT_CELL_INFO =
            "INSERT INTO cell_info(test_uid, time, type, arfcn_number, result_band, result_band_name, result_frequency_download, result_frequency_upload, result_bandwidth) "
                    + "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // SQL select
    public static final String SELECT_CELL_INFO_FOT_TESTUID =
            "SELECT test_uid, time, type, arfcn_number, result_band, result_band_name, result_frequency_download, result_frequency_upload, result_bandwidth FROM CELL_INFO "
                    + " WHERE test_uid = ? ORDER BY time, test_uid ASC";

    private long uid;
    private long test_uid;
    private long time;
    private String type;
    private int arfcnNumber;
    private int resultBand;
    private String resultBandName;
    private double resultFrequencyDownload;
    private double resultFrequencyUpload;
    private double resultBandwidth;

    private Connection conn = null;
    private String errorLabel = "";
    private boolean error = false;

    public CellInfo() {
        super();
    }

    public CellInfo(final Connection conn) {
        reset();
        this.conn = conn;
    }

    public CellInfo(Connection conn, long uid, long test_uid, long time, String type, int arfcnNumber, int resultBand,
                    String resultBandName, double resultFrequencyDownload, double resultFrequencyUpload, double resultBandwidth) {

        reset();

        this.conn = conn;

        this.uid = uid;
        this.test_uid = test_uid;
        this.time = time;
        this.type = type;
        this.arfcnNumber = arfcnNumber;
        this.resultBand = resultBand;
        this.resultBandName = resultBandName;
        this.resultFrequencyDownload = resultFrequencyDownload;
        this.resultFrequencyUpload = resultFrequencyUpload;
        this.resultBandwidth = resultBandwidth;
    }

    public void reset() {

        uid = 0;
        test_uid = 0;
        time = 0L;
        type = null;
        arfcnNumber = 0;
        resultBand = 0;
        resultBandName = null;
        resultFrequencyDownload = 0;
        resultFrequencyUpload = 0;
        resultBandwidth = 0;

        resetError();
    }

    private void resetError() {
        error = false;
        errorLabel = "";
    }

    private void setError(final String errorLabel) {
        error = true;
        this.errorLabel = errorLabel;
    }

    public void storeCellInfo() {
        PreparedStatement st;
        try {
            st = conn.prepareStatement(
                    SQL_INSERT_CELL_INFO, Statement.RETURN_GENERATED_KEYS);

            SQLHelper.setLongOrNull(st, 1, test_uid);
            SQLHelper.setLongOrNull(st, 2, time);
            SQLHelper.setStringOrNull(st, 3, type);
            SQLHelper.setIntOrNull(st, 4, arfcnNumber);
            SQLHelper.setIntOrNull(st, 5, resultBand);
            SQLHelper.setStringOrNull(st, 6, resultBandName);
            SQLHelper.setDoubleOrNull(st, 7, resultFrequencyDownload);
            SQLHelper.setDoubleOrNull(st, 8, resultFrequencyUpload);
            SQLHelper.setDoubleOrNull(st, 9, resultBandwidth);

            logger.debug(st.toString());
            final int affectedRows2 = st.executeUpdate();
            if (affectedRows2 == 0)
                setError("ERROR_DB_STORE_CELLLOCATION");
            else {
                final ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    // Retrieve the auto generated key(s).
                    uid = rs.getInt(1);
                }
                SQLHelper.closeResultSet(rs);
            }

            // close prepared statement
            SQLHelper.closePreparedStatement(st);

        } catch (final SQLException e) {
            setError("ERROR_DB_STORE_CELLLOCATION_SQL");
            logger.error(e.getMessage());
        }
    }

    public boolean hasError() {
        return error;
    }

    public String getError() {
        return errorLabel;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getTest_uid() {
        return test_uid;
    }

    public void setTest_uid(long test_uid) {
        this.test_uid = test_uid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getArfcnNumber() {
        return arfcnNumber;
    }

    public void setArfcnNumber(int arfcnNumber) {
        this.arfcnNumber = arfcnNumber;
    }

    public int getResultBand() {
        return resultBand;
    }

    public void setResultBand(int resultBand) {
        this.resultBand = resultBand;
    }

    public String getResultBandName() {
        return resultBandName;
    }

    public void setResultBandName(String resultBandName) {
        this.resultBandName = resultBandName;
    }

    public double getResultFrequencyDownload() {
        return resultFrequencyDownload;
    }

    public void setResultFrequencyDownload(double resultFrequencyDownload) {
        this.resultFrequencyDownload = resultFrequencyDownload;
    }

    public double getResultFrequencyUpload() {
        return resultFrequencyUpload;
    }

    public void setResultFrequencyUpload(double resultFrequencyUpload) {
        this.resultFrequencyUpload = resultFrequencyUpload;
    }

    public double getResultBandwidth() {
        return resultBandwidth;
    }

    public void setResultBandwidth(double resultBandwidth) {
        this.resultBandwidth = resultBandwidth;
    }
}
