package at.alladin.rmbt.controlServer;

import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class HealthMonitorResource extends ServerResource {

    private static final Logger logger = LoggerFactory.getLogger(BadgeResource.class);

    private static final java.util.List<String> JDBC_ATTRIBUTES = new java.util.ArrayList<>(java.util.Arrays.asList("jmxName", "numActive", "numIdle"));

    @Get()
    public String retrieve(final String entity) {

        addAllowOrigin();

        String clientIpRaw = getIP();

        logger.debug("rquest HealthMonitor from: " + clientIpRaw);

        StringBuilder sb = new StringBuilder();

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = server.queryNames(null, null);
            for (ObjectName name : objectNames) {
                if (name.getCanonicalName().contains("type=DataSource")) {
                    sb.append(name.getCanonicalName()).append(":\n");
                    MBeanInfo info = server.getMBeanInfo(name);
                    sb.append("\tClass:").append(info.getClassName()).append("\n");
                    for (String attribute : JDBC_ATTRIBUTES) {
                        try {
                            Object attributeValue = server.getAttribute(name, attribute);
                            if (attributeValue != null) {
                                sb.append("\t").append(attribute).append(" : ").append(attributeValue.toString()).append("\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
