package com.mnnitproject.location_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_usage_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( updatable = false)
    @CreationTimestamp
    private LocalDateTime requestTimestamp;

    @Column(length = 20) // e.g., "IP_LOOKUP"
    private String requestType;

    @Column(length = 255) // Store the input IP
    private String requestInput;

    @Column(length = 50) // e.g., HTTP status code like "200_OK", "404_NOT_FOUND", "400_BAD_REQUEST"
    private String responseStatus;

    @Column(length = 100) // Could be the client's IP address if available
    private String clientIp;

    private Long processingTimeMs; // How long the request took to process

}