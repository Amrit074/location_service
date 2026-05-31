package com.mnnitproject.location_service.service;

import com.maxmind.geoip2.DatabaseReader;
import com.mnnitproject.location_service.dto.LocationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeoIPServiceImplTest {

    @Test
    void resolvesPublicIpFromBundledMaxMindDatabase() throws Exception {
        DatabaseReader reader = new DatabaseReader.Builder(
                new ClassPathResource("GeoLite2-City.mmdb").getInputStream()
        ).build();

        RedisTemplate<String, LocationResponse> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, LocationResponse> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        GeoIPService service = new GeoIPServiceImpl(reader, redisTemplate);

        LocationResponse response = service.findLocationByIp("8.8.8.8");

        assertThat(response.getIpAddress()).isEqualTo("8.8.8.8");
        assertThat(response.getCountry()).isNotBlank();
        assertThat(response.getLatitude()).isNotNull();
        assertThat(response.getLongitude()).isNotNull();
        assertThat(response.getMessage()).contains("MaxMind");
    }
}
