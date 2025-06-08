package com.mnnitproject.location_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

@Configuration
public class SQLiteConfig {

    @Value("${app.datasource.wof.url}")
    private String wofJdbcUrl;

    @Bean(name = "wofDataSource")
    public DataSource wofDataSource() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(wofJdbcUrl);
        return dataSource;
    }

    @Bean(name = "wofJdbcTemplate")
    public JdbcTemplate wofJdbcTemplate(@Qualifier("wofDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}