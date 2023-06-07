package at.alladin.rmbt.controlServer.OffOnNet.service;

import at.alladin.rmbt.db.DbConnection;
import at.alladin.rmbt.shared.SQLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

public class NetTestService {
    private static final Logger logger = LoggerFactory.getLogger(NetTestService.class);
    protected Connection connection;
    public NetTestService() throws IllegalStateException{
        try {
            connection = DbConnection.getConnection();
        } catch (final NamingException | SQLException e) {
            logger.error(e.getMessage());
            SQLHelper.closeConnection(this.connection);
            throw new IllegalStateException("connection to DB error");
        }
    }
    public void closeConnection() {
        SQLHelper.closeConnection(this.connection);
    }
}
