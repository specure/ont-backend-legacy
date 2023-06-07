package at.alladin.rmbt.qosadmin.config;

import at.alladin.rmbt.qosadmin.config.db.QoSPersistenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Configuration
@Import({
        PropertyConfig.class,
        QoSPersistenceConfig.class
})
@ComponentScan(
        basePackages = "at.alladin.rmbt",
        excludeFilters = {
                @Filter(Configuration.class),
                @Filter(Controller.class)
        }
)
public class RootConfig {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(RootConfig.class);

}
