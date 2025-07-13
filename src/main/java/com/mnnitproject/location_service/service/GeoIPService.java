package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;
import org.springframework.cache.annotation.Cacheable;

public interface GeoIPService {
    @Cacheable(value = "ipLocationCache", key = "#ipAddress")
    LocationResponse findLocationByIp(String ipAddress);
}