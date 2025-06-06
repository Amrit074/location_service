package com.mnnitproject.location_service.controller;

import com.mnnitproject.location_service.dto.IpLocationRequest;
import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/ip")
    public ResponseEntity<LocationResponse> getLocationByIp(@Valid @RequestBody IpLocationRequest request, HttpServletRequest httpServletRequest) {

        String clientIp = httpServletRequest.getRemoteAddr();
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = httpServletRequest.getRemoteAddr();
            logger.debug("Client IP from remote address: {}", clientIp);
        } else {
            logger.debug("Client IP from X-Forwarded-For: {}", clientIp);
        }
        logger.info("Received IP lookup request for IP: {} from client: {}", request.getIp(), clientIp);

        LocationResponse response = locationService.lookupIpLocation(request, clientIp);

        return ResponseEntity.ok(response);
    }
}