package at.alladin.rmbt.qosadmin.config.db;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Configuration
@EnableJpaRepositories(
        transactionManagerRef = "transactionManagerQoS",
        entityManagerFactoryRef = "entityManagerFactoryQoS",
        basePackages = {"at.alladin.rmbt.qosadmin.repository"}
)
@EnableTransactionManagement
public class QoSPersistenceConfig implements DataSourceConfig {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(QoSPersistenceConfig.class);

    /**
     *
     */
    public static final String PERSISTANCE_UNIT_NAME = "qosUnit";

    /**
     *
     */
    @Value("${db.context}")
    public String dbContext;

    /**
     *
     */
    @Value("${hibernate.search.default.indexBase}")
    public String searchIndexBase;

    /**
     *
     */
    @Value("${hibernate.search.default.directory_provider}")
    public String searchDirectoryProvider;

    /*
     * (non-Javadoc)
     *
     * @see org.asteriskclient.ami.config.db.DataSourceConfig#dataSource()
     */
    @Bean(name = "dataSourceQoS")
    @Override
    public DataSource dataSource() throws NamingException {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup(dbContext);
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.austrianmint.showcase.wall.config.db.DataSourceConfig#jpaVendorAdapter()
     */
    @Bean(name = "jpaVendorAdapterQoS")
    @Override
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter() {

            /*
             * (non-Javadoc)
             * @see org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter#isGenerateDdl()
             */
            @Override
            protected boolean isGenerateDdl() {
                return false;
            }

            /*
             * (non-Javadoc)
             * @see org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter#isShowSql()
             */
            @Override
            protected boolean isShowSql() {
                return false; /* true */
            }

            /*
             * (non-Javadoc)
             * @see org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter#getDatabasePlatform()
             */
            @Override
            protected String getDatabasePlatform() {
                return PostgreSQL9Dialect.class.getName();
            }
        };
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.austrianmint.showcase.wall.config.db.DataSourceConfig#jpaProperties()
     */
    @Bean(name = "jpaPropertiesQoS")
    @Override
    public Map<String, Object> jpaProperties() {
        Map<String, Object> jpaProperties = new HashMap<>();

        jpaProperties.put("hibernate.search.default.directory_provider", searchDirectoryProvider);
        jpaProperties.put("hibernate.search.default.indexBase", searchIndexBase);

        return jpaProperties;
    }


    /**
     * @return
     * @throws NamingException
     */
    @Bean(name = "entityManagerFactoryQoS")
    //@DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws NamingException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("at.alladin.rmbt.qosadmin.model");

        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        entityManagerFactoryBean.setJpaPropertyMap(jpaProperties());
        entityManagerFactoryBean.setPersistenceUnitName(getPersistenceUnitName());

        return entityManagerFactoryBean;
    }

    /**
     * @return
     * @throws NamingException
     */
    @Bean(name = "transactionManagerQoS")
    public PlatformTransactionManager txManager() {
        try {
            return new JpaTransactionManager(entityManagerFactory().getObject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see at.alladin.weisungsdb.config.db.DataSourceConfig#getPersistenceUnitName()
     */
    @Override
    public String getPersistenceUnitName() {
        return PERSISTANCE_UNIT_NAME;
    }
}
