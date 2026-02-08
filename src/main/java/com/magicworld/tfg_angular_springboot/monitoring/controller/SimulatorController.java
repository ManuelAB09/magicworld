package com.magicworld.tfg_angular_springboot.monitoring.controller;

import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import com.magicworld.tfg_angular_springboot.monitoring.simulator.ParkSimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring/simulator")
@RequiredArgsConstructor
@Tag(name = "Simulator", description = "Park simulator controls")
public class SimulatorController {

    private final ParkSimulatorService simulatorService;
    private final DashboardService dashboardService;
    private final MonitoringWebSocketService webSocketService;

    @Operation(summary = "Start the park simulator")
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start() {
        simulatorService.start();
        return ResponseEntity.ok(simulatorService.getSimulatorStatus());
    }

    @Operation(summary = "Stop the park simulator")
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stop() {
        simulatorService.stop();
        return ResponseEntity.ok(simulatorService.getSimulatorStatus());
    }

    @Operation(summary = "Get simulator status")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(simulatorService.getSimulatorStatus());
    }

    @Operation(summary = "Force broadcast current dashboard")
    @PostMapping("/broadcast")
    public ResponseEntity<Void> forceBroadcast() {
        webSocketService.broadcastDashboard(dashboardService.getSnapshot());
        return ResponseEntity.ok().build();
    }
}
