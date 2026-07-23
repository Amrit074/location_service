package com.mnnitproject.location_service.controller;

import com.mnnitproject.location_service.dto.GPSLocationRequest;
import com.mnnitproject.location_service.dto.IpLocationRequest;
import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.service.LocationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @GetMapping("/")
    public void redirectRoot(HttpServletResponse response) throws IOException {
        response.sendRedirect("/userform.html");
    }

    @PostMapping("/ip")
    public ResponseEntity<LocationResponse> getLocationByIp(@Valid @RequestBody IpLocationRequest request,
                                                            HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        logger.info("Received IP lookup request for IP: {} from client: {}", request.getIp(), clientIp);

        LocationResponse response = locationService.lookupIpLocation(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/gps")
    public ResponseEntity<LocationResponse> getLocationByGps(@Valid @RequestBody GPSLocationRequest request,
                                                             HttpServletRequest httpServletRequest) {

        String clientIp = getClientIp(httpServletRequest);

        logger.info("Received GPS lookup request for Lat: {}, Lon: {} from client: {}",
                request.getLatitude(), request.getLongitude(), clientIp);

        LocationResponse response = locationService.lookupGPSLocation(request, clientIp);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            String clientIp = xForwardedFor.split(",")[0].trim();
            logger.debug("Resolved Client IP from X-Forwarded-For: {} (Original Header: {})", clientIp, xForwardedFor);
            return clientIp;
        }

        logger.debug("Resolved Client IP from Remote Address: {}", remoteAddr);
        return remoteAddr;
    }
}