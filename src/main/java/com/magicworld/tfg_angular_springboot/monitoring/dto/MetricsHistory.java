package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MetricsHistory {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<MetricsPoint> dataPoints;
}
