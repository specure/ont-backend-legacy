package at.alladin.rmbt.qosadmin.config.db;

import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface DataSourceConfig {

    /**
     * @return
     */
    public String getPersistenceUnitName();

    /**
     * @return
     * @throws Exception
     */
    @Bean
    public DataSource dataSource() throws Exception;

    /**
     * @return
     */
    @Bean
    public JpaVendorAdapter jpaVendorAdapter();

    /**
     * @return
     */
    @Bean
    public Map<String, Object> jpaProperties();

    /**
     * @throws Exception
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws Exception;

    /**
     * @return
     */
    @Bean
    public PlatformTransactionManager txManager();
}
