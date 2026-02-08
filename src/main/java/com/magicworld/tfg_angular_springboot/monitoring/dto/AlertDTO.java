package com.magicworld.tfg_angular_springboot.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertSeverity;
import com.magicworld.tfg_angular_springboot.monitoring.alert.AlertType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AlertDTO {
    private Long id;
    private AlertType alertType;
    private AlertSeverity severity;
    private String message;
    private String suggestion;
    private Long attractionId;
    private String attractionName;
    private LocalDateTime timestamp;
    @JsonProperty("isActive")
    private boolean active;
    private List<ResolutionOption> resolutionOptions;
}
