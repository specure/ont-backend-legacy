package at.alladin.rmbt.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SQLHelper {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(SQLHelper.class);

    public static void setBooleanOrNull(PreparedStatement pstmt, int column, Boolean value)
            throws SQLException {
        if (value != null) {
            pstmt.setBoolean(column, value);
        } else {
            pstmt.setNull(column, Types.BOOLEAN);
        }
    }

    public static void setDoubleOrNull(PreparedStatement pstmt, int column, Double value)
            throws SQLException {
        if (value != null) {
            pstmt.setDouble(column, value);
        } else {
            pstmt.setNull(column, Types.DOUBLE);
        }
    }

    public static void setFloatOrNull(PreparedStatement pstmt, int column, Float value)
            throws SQLException {
        if (value != null) {
            pstmt.setFloat(column, value);
        } else {
            pstmt.setNull(column, Types.FLOAT);
        }
    }

    public static void setIntOrNull(PreparedStatement pstmt, int column, Integer value)
            throws SQLException {
        if (value != null) {
            pstmt.setInt(column, value);
        } else {
            pstmt.setNull(column, java.sql.Types.INTEGER);
        }
    }

    public static void setLongOrNull(PreparedStatement pstmt, int column, Long value)
            throws SQLException {
        if (value != null) {
            pstmt.setLong(column, value);
        } else {
            pstmt.setNull(column, Types.BIGINT);
        }
    }

    public static void setStringOrNull(PreparedStatement pstmt, int column, String value)
            throws SQLException {
        if (value != null && value.isEmpty() == false) {
            pstmt.setString(column, value);
        } else {
            pstmt.setNull(column, Types.VARCHAR);
        }
    }

    public static void setTimestampOrNull(PreparedStatement pstmt, int column, Timestamp value)
            throws SQLException {
        if (value != null && value instanceof Timestamp) {
            pstmt.setTimestamp(column, value);
        } else {
            pstmt.setNull(column, Types.TIMESTAMP);
        }
    }

    public static void setTimestampOrNull(PreparedStatement pstmt, int column, Long value)
            throws SQLException {
        if (value != null) {
            pstmt.setTimestamp(column, new Timestamp(value));
        } else {
            pstmt.setNull(column, Types.TIMESTAMP);
        }
    }

    public static synchronized void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null && resultSet.isClosed() == false) {
                //logger.debug("Close result set...");
                resultSet.close();
                //logger.debug("Close result set... DONE");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static synchronized void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null && preparedStatement.isClosed() == false) {
                //logger.debug("Close prepared statement...");
                preparedStatement.close();
                //logger.debug("Close prepared statement... DONE");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static synchronized void closeConnection(Connection connection) {
        try {
            if (connection != null && connection.isClosed() == false) {
                //logger.debug("Close connection...");
                connection.close();
                //logger.debug("Close connection... DONE");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

}
