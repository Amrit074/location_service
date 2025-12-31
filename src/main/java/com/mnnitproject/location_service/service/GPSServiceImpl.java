package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GPSServiceImpl implements GPSService {

    private static final Logger logger = LoggerFactory.getLogger(GPSServiceImpl.class);

    @Override
    public LocationResponse findLocationByGPS(Double latitude, Double longitude) {
        logger.warn("GPS Lookup disabled in Cloud deployment.");
        throw new ResourceNotFoundException("GPS Lookup is disabled in this lightweight cloud deployment.");
    }
}