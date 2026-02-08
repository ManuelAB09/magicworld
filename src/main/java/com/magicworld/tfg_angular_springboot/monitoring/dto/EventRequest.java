package com.magicworld.tfg_angular_springboot.monitoring.dto;

import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventRequest {
    @NotNull
    private ParkEventType eventType;
    private Long attractionId;
    private Long userId;
    private Integer visitorCount;
    private Integer queueSize;
    private String metadata;
}
