package com.mnnitproject.location_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpLocationRequest {
    @NotBlank(message = "IP can not be blank")
    private String ip;

}
