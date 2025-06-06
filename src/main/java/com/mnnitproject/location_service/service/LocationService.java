package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.IpLocationRequest;
import com.mnnitproject.location_service.dto.LocationResponse;

public interface LocationService {
    LocationResponse lookupIpLocation(IpLocationRequest request, String clientIp);
}