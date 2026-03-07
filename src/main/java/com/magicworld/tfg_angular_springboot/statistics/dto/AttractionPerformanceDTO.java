package com.magicworld.tfg_angular_springboot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttractionPerformanceDTO {
    private Long attractionId;
    private String attractionName;
    private long totalQueueEvents;
    private int maxQueueSize;
    private double avgQueueSize;
}

