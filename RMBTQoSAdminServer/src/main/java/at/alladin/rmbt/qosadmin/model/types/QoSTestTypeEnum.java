package at.alladin.rmbt.qosadmin.model.types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public class QoSTestTypeEnum extends GenericEnumType<String, QoSTestType> {

    public QoSTestTypeEnum() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        super(QoSTestType.class, QoSTestType.values(), "getValue", Types.OTHER);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
                              SessionImplementor session, Object owner)
            throws HibernateException, SQLException {
        return nullSafeGet(rs, names, owner);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index,
                            SessionImplementor session) throws HibernateException, SQLException {
        nullSafeSet(st, value, index);
    }

}
