package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.exceptions.InvalidInputException;
import com.mnnitproject.location_service.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GPSServiceImpl implements GPSService {

    private static final Logger logger = LoggerFactory.getLogger(GPSServiceImpl.class);

    private final JdbcTemplate wofJdbcTemplate;

    public GPSServiceImpl(@Qualifier("wofJdbcTemplate") JdbcTemplate wofJdbcTemplate) {
        this.wofJdbcTemplate = wofJdbcTemplate;
    }

    @Override
    public LocationResponse findLocationByGPS(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            logger.warn("Received null latitude or longitude for GPS lookup.");
            throw new InvalidInputException("Latitude and longitude cannot be null for GPS lookup.");
        }

        logger.warn("GPS location lookup service is attempting to query local Who's On First SQLite for coordinates: {}, {}. Be aware of performance with large databases and potential need for SpatiaLite.",
                latitude, longitude);

        String simpleWofQuery = """
            SELECT
                id,
                name,
                placetype,
                country,
                min_latitude,
                max_latitude,
                min_longitude,
                max_longitude
            FROM
                spr
            WHERE
                ? BETWEEN min_latitude AND max_latitude AND
                ? BETWEEN min_longitude AND max_longitude AND
                placetype = 'locality'
            LIMIT 1;
            """;

        try {
            List<Map<String, Object>> results = wofJdbcTemplate.queryForList(simpleWofQuery,
                    latitude,
            longitude);

            if (!results.isEmpty()) {
                Map<String, Object> result = results.getFirst();

                String cityName = (String) result.get("name");
                String countryCode = (String) result.get("country");
                Double resultLat = (Double) result.get("min_latitude");
                Double resultLon = (Double) result.get("min_longitude");

                return LocationResponse.builder()
                        .latitude(resultLat != null ? resultLat : latitude)
                        .longitude(resultLon != null ? resultLon : longitude)
                        .city(cityName)
                        .country(countryCode)
                        .message("Location found from local WOF DB (bounding box check).")
                        .build();
            } else {
                logger.warn("Location not found in local WOF database for Lat: {}, Lon: {}", latitude, longitude);
                throw new ResourceNotFoundException("Location not found for given coordinates in local Who's On First database.");
            }
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Error querying local WOF database for GPS coordinates ({}, {}): {}", latitude, longitude, e.getMessage(), e);
            throw new RuntimeException("Error accessing local Who's On First database for GPS lookup.", e);
        }
    }
}