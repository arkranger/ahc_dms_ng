/*
 ****************************
 * * * PRIMARY DATABASE * * *
 ****************************
 */
package ahc.dms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;

@Configuration
@PropertySource({ "classpath:persistence-dev.properties" })
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ahc.dms.dao.dms.repositories",
        entityManagerFactoryRef = "dmsEntityManager",
        transactionManagerRef = "dmsTransactionManager"
)
public class DmsConfig {

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "dmsDataSource")
    public DataSource dmsDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.dms.driverClassName")));
        dataSource.setUrl(env.getProperty("spring.datasource.dms.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.dms.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.dms.password"));

        return dataSource;
    }

    @Primary
    @Bean(name = "dmsEntityManager")
    public LocalContainerEntityManagerFactoryBean dmsEntityManager() {

        HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.dms.properties.hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.dms.properties.hibernate.dialect"));
        jpaProperties.put("hibernate.show_sql", "spring.jpa.dms.properties.show-sql");
        jpaProperties.put("hibernate.format_sql", "spring.jpa.dms.properties.format-sql");

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dmsDataSource());
        emf.setPackagesToScan("ahc.dms.dao.dms.entities");
        emf.setJpaPropertyMap(jpaProperties);
        emf.setPersistenceUnitName("dms");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return emf;
    }

    @Primary
    @Bean(name = "dmsTransactionManager")
    public PlatformTransactionManager dmsTransactionManager() {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(dmsEntityManager().getObject());
        return transactionManager;
    }

}