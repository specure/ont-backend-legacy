package at.alladin.rmbt.qosadmin.util;

import at.alladin.rmbt.qosadmin.model.TestServer;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public class TestServerUtil {

    public static String getTestServerName(TestServer t) {
        return (t != null ? t.getUid() + " - " + t.getName() : null);
    }
}
