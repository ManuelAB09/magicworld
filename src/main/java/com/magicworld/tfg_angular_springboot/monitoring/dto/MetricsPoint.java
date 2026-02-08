package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MetricsPoint {
    private LocalDateTime timestamp;
    private int visitors;
    private int queueSize;
    private int waitTimeMinutes;
}
