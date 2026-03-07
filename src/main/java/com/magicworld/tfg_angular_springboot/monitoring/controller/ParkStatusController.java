package com.magicworld.tfg_angular_springboot.monitoring.controller;

import com.magicworld.tfg_angular_springboot.monitoring.dto.AttractionStatus;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.simulator.ParkSimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Public endpoints for park live status (queue levels visible on the park map for all users).
 */
@RestController
@RequestMapping("/api/v1/park-status")
@RequiredArgsConstructor
@Tag(name = "Park Status", description = "Public park live status endpoints")
public class ParkStatusController {

    private final ParkSimulatorService simulatorService;
    private final DashboardService dashboardService;

    @Operation(summary = "Get simulator running status (public)")
    @GetMapping("/simulator")
    public ResponseEntity<Map<String, Object>> getSimulatorStatus() {
        return ResponseEntity.ok(simulatorService.getSimulatorStatus());
    }

    @Operation(summary = "Get live attraction queue statuses (public)")
    @GetMapping("/attractions")
    public ResponseEntity<List<AttractionStatus>> getAttractionStatuses() {
        List<AttractionStatus> statuses = dashboardService.getSnapshot().getAttractionStatuses();
        return ResponseEntity.ok(statuses);
    }
}

