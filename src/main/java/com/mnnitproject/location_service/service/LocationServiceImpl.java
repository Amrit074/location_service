package com.mnnitproject.location_service.service;

import com.mnnitproject.location_service.dto.GPSLocationRequest;
import com.mnnitproject.location_service.dto.IpLocationRequest;
import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.entity.ApiUsageLog;
import com.mnnitproject.location_service.repository.ApiUsageLogRepository;
import com.mnnitproject.location_service.exceptions.InvalidInputException;
import com.mnnitproject.location_service.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final GeoIPService geoIPService;
    private final ApiUsageLogRepository apiUsageLogRepository;
    private final GPSService gpsService;

    @Autowired
    public LocationServiceImpl(GeoIPService geoIPService, GPSService gpsService, ApiUsageLogRepository apiUsageLogRepository) {
        this.geoIPService = geoIPService;
        this.apiUsageLogRepository = apiUsageLogRepository;
        this.gpsService = gpsService;
    }

    @Override
    public LocationResponse lookupIpLocation(IpLocationRequest request, String clientIp) {
        long startTime = System.currentTimeMillis();
        String status = "UNKNOWN_STATUS";
        LocationResponse response = null;
        String ipInput = (request != null && request.getIp() != null) ? request.getIp() : "N/A";

        try {
            if (request == null || request.getIp() == null || request.getIp().trim().isEmpty()) {
                logger.warn("Received null or empty IpLocationRequest or IP address from client: {}", clientIp);
                status = "400_BAD_REQUEST";
                throw new InvalidInputException("IP address must be provided in the request.");
            }

            logger.info("Calling GeoIPService to lookup IP: {} from client: {}", request.getIp(), clientIp);
            response = geoIPService.findLocationByIp(request.getIp());

            status = "200_OK_FOUND";
            return response;

        } catch (InvalidInputException e) {
            status = "400_BAD_REQUEST";
            logger.warn("Invalid input for IP {} from client {}: {}", ipInput, clientIp, e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            status = "404_NOT_FOUND";
            logger.warn("Resource not found for IP {} from client {}: {}", ipInput, clientIp, e.getMessage());
            throw e;
        } catch (Exception e) {
            status = "500_INTERNAL_SERVER_ERROR";
            logger.error("Unexpected error during IP location processing for IP {} from client {}: {}", ipInput, clientIp, e.getMessage(), e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            logApiUsage("IP_LOOKUP", ipInput, status, clientIp, endTime - startTime);
        }
    }

    @Override
    public LocationResponse lookupGPSLocation(GPSLocationRequest request, String clientIp){
        long startTime = System.currentTimeMillis();
        String status = "UNKNOWN_STATUS";
        LocationResponse response = null;
        String gpsInput = (request != null && request.getLatitude() != null && request.getLongitude() != null) ?
                ("Lat:" + request.getLatitude() + ",Lon:" + request.getLongitude()) : "N/A_GPS";

        try {

            if (request == null) {
                logger.warn("Received null GpsLocationRequest from client: {}", clientIp);
                status = "400_BAD_REQUEST";
                throw new InvalidInputException("GPS location request cannot be null.");
            }

            logger.info("Calling GPSService to lookup GPS: {} from client: {}", gpsInput, clientIp);
            response = gpsService.findLocationByGPS(request.getLatitude(), request.getLongitude());

            status = "200_OK_FOUND";
            return response;

        } catch (InvalidInputException e) {
            status = "400_BAD_REQUEST";
            logger.warn("Invalid input for GPS {} from client {}: {}", gpsInput, clientIp, e.getMessage());
            throw e;
        } catch (Exception e) {
            status = "500_INTERNAL_SERVER_ERROR";
            logger.error("Unexpected error during GPS location processing for GPS {} from client {}: {}", gpsInput, clientIp, e.getMessage(), e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            logApiUsage("GPS_LOOKUP", gpsInput, status, clientIp, endTime - startTime);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void logApiUsage(String requestType, String requestInput, String responseStatus, String clientIp, long processingTimeMs) {
        try {
            ApiUsageLog logEntry = ApiUsageLog.builder()
                    .requestType(requestType)
                    .requestInput(requestInput)
                    .responseStatus(responseStatus)
                    .clientIp(clientIp)
                    .processingTimeMs(processingTimeMs)
                    .build();
            apiUsageLogRepository.save(logEntry);
            logger.debug("API usage log saved: Type={}, Input={}, Status={}, ClientIP={}", requestType, requestInput, responseStatus, clientIp);
        } catch (Exception e) {
            logger.error("Failed to save API usage log for Type={}, Input={}: {}", requestType, requestInput, e.getMessage(), e);
        }
    }
}