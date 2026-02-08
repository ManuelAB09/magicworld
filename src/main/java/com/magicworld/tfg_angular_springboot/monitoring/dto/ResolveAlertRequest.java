package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveAlertRequest {
    private String resolutionOptionId;
    private String notes;
}
