package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardSnapshot {
    private int currentVisitors;
    private int totalEntriesToday;
    private int totalSalesToday;
    private int activeAttractions;
    private int totalAttractions;
    private double avgParkWaitTime;
    private int ticketsSoldToday;
    private int parkMaxCapacity;
    private List<AttractionStatus> attractionStatuses;
    private List<AlertDTO> activeAlerts;
}
