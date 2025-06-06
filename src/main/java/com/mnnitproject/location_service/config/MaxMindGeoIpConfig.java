package com.mnnitproject.location_service.config;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class MaxMindGeoIpConfig {

    private static final Logger logger = LoggerFactory.getLogger(MaxMindGeoIpConfig.class);

    @Value("${maxmind.geoip.database.path}")
    private Resource geoIpDatabaseResource;

    @Bean
    @Qualifier("maxmindDatabaseReader")
    public DatabaseReader maxmindDatabaseReader() throws IOException {
        if (!geoIpDatabaseResource.exists()) {
            logger.error("MaxMind GeoLite2-City.mmdb database not found at: {}", geoIpDatabaseResource.getURI());
            throw new IOException("MaxMind GeoLite2-City.mmdb database not found at: " + geoIpDatabaseResource.getURI());
        }
        try (InputStream ipDatabaseStream = geoIpDatabaseResource.getInputStream()) {
            DatabaseReader reader = new DatabaseReader.Builder(ipDatabaseStream).build();
            logger.info("MaxMind GeoLite2-City.mmdb loaded successfully from: {}", geoIpDatabaseResource.getURI());
            return reader;
        } catch (IOException e) {
            logger.error("Failed to load MaxMind GeoLite2-City.mmdb database: {}", e.getMessage(), e);
            throw new IOException("Failed to load MaxMind GeoLite2-City.mmdb database: " + e.getMessage(), e);
        }
    }
}