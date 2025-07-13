package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;
import org.springframework.cache.annotation.Cacheable;

public interface GPSService {
    @Cacheable(value = "gpsLocationCache", key = "#latitude + '-' + #longitude")
    LocationResponse findLocationByGPS(Double latitude, Double longitude);
}
