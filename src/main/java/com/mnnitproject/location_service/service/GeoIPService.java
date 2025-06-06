package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;

public interface GeoIPService {
    LocationResponse findLocationByIp(String ipAddress);
}