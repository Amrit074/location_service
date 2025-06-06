package com.mnnitproject.location_service.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.exceptions.InvalidInputException; // Import custom exception
import com.mnnitproject.location_service.exceptions.ResourceNotFoundException; // Import custom exception
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class GeoIPServiceImpl implements GeoIPService {

    private static final Logger logger = LoggerFactory.getLogger(GeoIPServiceImpl.class);
    private final DatabaseReader maxmindDatabaseReader;

    @Autowired
    public GeoIPServiceImpl(@Qualifier("maxmindDatabaseReader") DatabaseReader databaseReader) {
        this.maxmindDatabaseReader = databaseReader;
    }

    @Override
    public LocationResponse findLocationByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            logger.warn("Attempt to look up null or empty IP address.");
            throw new InvalidInputException("IP address cannot be null or empty.");
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse geoIpResponse = maxmindDatabaseReader.city(inetAddress);

            if (geoIpResponse == null) {
                logger.warn("No GeoIP response object for IP: {}", ipAddress);
                throw new ResourceNotFoundException("Location details not found for IP: " + ipAddress + " (no GeoIP response).");
            }


            com.maxmind.geoip2.record.Location location = geoIpResponse.getLocation();
            com.maxmind.geoip2.record.City cityRecord = geoIpResponse.getCity();
            com.maxmind.geoip2.record.Subdivision subdivisionRecord = geoIpResponse.getMostSpecificSubdivision();
            com.maxmind.geoip2.record.Country countryRecord = geoIpResponse.getCountry();

            return LocationResponse.builder()
                    .ipAddress(ipAddress)
                    .city(cityRecord != null ? cityRecord.getName() : null)
                    .state(subdivisionRecord != null ? subdivisionRecord.getName() : null)
                    .country(countryRecord != null ? countryRecord.getName() : null)
                    .latitude(location != null ? location.getLatitude() : null)
                    .longitude(location != null ? location.getLongitude() : null)
                    .message("Location found.")
                    .build();

        } catch (UnknownHostException e) {
            logger.warn("Invalid IP address format: {} - {}", ipAddress, e.getMessage());
            throw new InvalidInputException("Invalid IP address format: " + ipAddress);
        } catch (AddressNotFoundException e) {
            logger.info("Location not found in MaxMind database for IP: {}", ipAddress);
            throw new ResourceNotFoundException("Location not found for IP address: " + ipAddress);
        } catch (GeoIp2Exception | IOException e) {
            logger.error("Error looking up IP address {}: {}", ipAddress, e.getMessage(), e);
            throw new RuntimeException("An error occurred during GeoIP lookup for IP: " + ipAddress, e);
        }
    }
}