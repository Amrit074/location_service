package com.mnnitproject.location_service.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.mnnitproject.location_service.repository",
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MySQLJpaConfig {

    @Value("${spring.datasource.url}")
    private String mysqlJdbcUrl;
    @Value("${spring.datasource.username}")
    private String mysqlUsername;
    @Value("${spring.datasource.password}")
    private String mysqlPassword;
    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    @Value("${spring.jpa.database-platform}")
    private String hibernateDialect;
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;
    @Value("${spring.jpa.show-sql}")
    private boolean showSql;
    @Value("${spring.jpa.properties.hibernate.format_sql}")
    private boolean formatSql;


    @Primary
    @Bean(name = "mysqlDataSource")
    public HikariDataSource mysqlDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(mysqlJdbcUrl);
        dataSource.setUsername(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        dataSource.setDriverClassName(mysqlDriverClassName);
        return dataSource;
    }

    @Primary
    @Bean(name = "mysqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource
    ) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(mysqlDataSource);
        em.setPackagesToScan("com.mnnitproject.location_service.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", hibernateDialect);
        properties.put("hibernate.hbm2ddl.auto", ddlAuto);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.format_sql", formatSql);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean(name = "mysqlTransactionManager")
    public JpaTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory mysqlEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(mysqlEntityManagerFactory);
        return transactionManager;
    }
}