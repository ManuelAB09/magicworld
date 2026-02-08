package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MetricsExportRow {
    private LocalDateTime timestamp;
    private String attractionName;
    private Integer queueSize;
    private Integer waitTimeMinutes;
    private Integer visitors;
    private Integer ridesCompleted;
}
