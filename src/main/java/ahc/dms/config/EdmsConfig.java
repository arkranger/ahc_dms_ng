/*
******************************
* * * SECONDARY DATABASE * * *
******************************
*/

package ahc.dms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "ahc.dms.dao.edms.repositories",
        entityManagerFactoryRef = "edmsEntityManager",
        transactionManagerRef = "edmsTransactionManager"
)
public class EdmsConfig {

    @Autowired
    private Environment env;

    @Bean(name = "edmsDataSource")
    public DataSource edmsDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("spring.datasource.edms.driverClassName")));
        dataSource.setUrl(env.getProperty("spring.datasource.edms.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.edms.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.edms.password"));

        return dataSource;
    }

    @Bean(name = "edmsEntityManager")
    public LocalContainerEntityManagerFactoryBean edmsEntityManager() {

        HashMap<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.edms.properties.hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.dialect", env.getProperty("spring.jpa.edms.properties.hibernate.dialect"));
        jpaProperties.put("hibernate.show_sql", "spring.jpa.edms.properties.show-sql");
        jpaProperties.put("hibernate.format_sql", "spring.jpa.edms.properties.format-sql");

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(edmsDataSource());
        emf.setPackagesToScan("ahc.dms.dao.edms.entities");
        emf.setJpaPropertyMap(jpaProperties);
        emf.setPersistenceUnitName("edms");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return emf;
    }

    @Bean(name = "edmsTransactionManager")
    public PlatformTransactionManager edmsTransactionManager() {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(edmsEntityManager().getObject());
        return transactionManager;
    }
}