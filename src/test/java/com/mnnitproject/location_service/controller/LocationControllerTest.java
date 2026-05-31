package com.mnnitproject.location_service.controller;

import com.mnnitproject.location_service.dto.LocationResponse;
import com.mnnitproject.location_service.service.LocationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({LocationController.class, HealthController.class})
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void validIpReturnsLocationResponse() throws Exception {
        Mockito.when(locationService.lookupIpLocation(any(), anyString()))
                .thenReturn(LocationResponse.builder()
                        .ipAddress("8.8.8.8")
                        .city("Mountain View")
                        .state("California")
                        .country("United States")
                        .latitude(37.386)
                        .longitude(-122.0838)
                        .message("Source: MaxMind Database")
                        .build());

        mockMvc.perform(post("/api/location/ip")
                        .contentType("application/json")
                        .content("{\"ip\":\"8.8.8.8\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ipAddress").value("8.8.8.8"))
                .andExpect(jsonPath("$.country").value("United States"));
    }

    @Test
    void invalidIpReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/location/ip")
                        .contentType("application/json")
                        .content("{\"ip\":\"8.8.8\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
