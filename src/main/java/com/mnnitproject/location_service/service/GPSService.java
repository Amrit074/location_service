package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;

public interface GPSService {
    LocationResponse findLocationByGPS(Double latitude, Double longitude);
}
