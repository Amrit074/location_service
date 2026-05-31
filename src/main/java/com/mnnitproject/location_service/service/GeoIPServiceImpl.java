package com.mnnitproject.location_service.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.exceptions.InvalidInputException;
import com.mnnitproject.location_service.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

@Service
public class GeoIPServiceImpl implements GeoIPService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIPServiceImpl.class);

    private static final String CACHE_KEY_PREFIX = "geoip_v1:";

    private static final Duration CACHE_DURATION = Duration.ofHours(24);

    private final DatabaseReader maxmindDatabaseReader;
    private final RedisTemplate<String, LocationResponse> redisTemplate;

    @Autowired
    public GeoIPServiceImpl(@Qualifier("maxmindDatabaseReader") DatabaseReader databaseReader,
                            RedisTemplate<String, LocationResponse> redisTemplate) {
        this.maxmindDatabaseReader = databaseReader;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public LocationResponse findLocationByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new InvalidInputException("IP address cannot be null or empty.");
        }

        String cacheKey = CACHE_KEY_PREFIX + ipAddress;

        try {
            LocationResponse cachedResponse = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResponse != null) {
                logger.info("⚡ CACHE HIT: Found location for IP {} in Redis.", ipAddress);
                return cachedResponse;
            }
        } catch (Exception e) {
            logger.error("⚠️ Redis cache unavailable. Proceeding to MaxMind DB. Error: {}", e.getMessage());
        }


        try {
            logger.info("💾 CACHE MISS: Querying MaxMind Local DB for IP {}", ipAddress);
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse geoIpResponse = maxmindDatabaseReader.city(inetAddress);

            if (geoIpResponse == null) {
                throw new ResourceNotFoundException("Location details not found for IP: " + ipAddress);
            }

            // Extract Data safely
            com.maxmind.geoip2.record.Location location = geoIpResponse.getLocation();
            com.maxmind.geoip2.record.City cityRecord = geoIpResponse.getCity();
            com.maxmind.geoip2.record.Subdivision subdivisionRecord = geoIpResponse.getMostSpecificSubdivision();
            com.maxmind.geoip2.record.Country countryRecord = geoIpResponse.getCountry();

            // Build Response
            LocationResponse response = LocationResponse.builder()
                    .ipAddress(ipAddress)
                    .city(cityRecord != null ? cityRecord.getName() : null)
                    .state(subdivisionRecord != null ? subdivisionRecord.getName() : null)
                    .country(countryRecord != null ? countryRecord.getName() : null)
                    .latitude(location != null ? location.getLatitude() : null)
                    .longitude(location != null ? location.getLongitude() : null)
                    .message("Source: MaxMind Database")
                    .build();

            try {
                redisTemplate.opsForValue().set(cacheKey, response, CACHE_DURATION);
                logger.debug("✅ Saved location for IP {} to Redis cache.", ipAddress);
            } catch (Exception e) {
                logger.error("Failed to write to Redis cache: {}", e.getMessage());
            }

            return response;

        } catch (UnknownHostException e) {
            throw new InvalidInputException("Invalid IP address format: " + ipAddress);
        } catch (AddressNotFoundException e) {
            throw new ResourceNotFoundException("IP not found in MaxMind database: " + ipAddress);
        } catch (GeoIp2Exception | IOException e) {
            logger.error("GeoIP lookup failed for IP {}: {}", ipAddress, e.getMessage(), e);
            throw new ResourceNotFoundException("Location lookup is temporarily unavailable for IP: " + ipAddress);
        }
    }
}
