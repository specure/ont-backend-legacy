package at.alladin.rmbt.qosadmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configuration class needed for config.properties bean
 *
 * @author alladin-IT (lb@alladin.at)
 */
@Configuration
@PropertySources({
        @PropertySource(value = "classpath:config/rmbtconfig.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:${catalina.home}/conf/rmbtconfig.properties")
})
public class PropertyConfig {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(PropertyConfig.class);

    /**
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        configurer.setFileEncoding("UTF-8");
        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(true);

        return configurer;
    }
}
