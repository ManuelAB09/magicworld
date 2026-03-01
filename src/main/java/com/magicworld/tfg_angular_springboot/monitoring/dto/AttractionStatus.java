package com.magicworld.tfg_angular_springboot.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttractionStatus {
    private Long attractionId;
    private String name;
    @JsonProperty("isOpen")
    private boolean open;
    private int queueSize;
    private int estimatedWaitMinutes;
    private double mapPositionX;
    private double mapPositionY;
    private String intensity;
}
