package com.mnnitproject.location_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLocationRequest {
    @NotBlank(message = "IP can not be blank")
    @Pattern(
            regexp = "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$",
            message = "IP must be a valid IPv4 address, for example 8.8.8.8"
    )
    private String ip;

}
