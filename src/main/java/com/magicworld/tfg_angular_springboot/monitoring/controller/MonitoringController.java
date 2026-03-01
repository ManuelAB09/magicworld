package com.magicworld.tfg_angular_springboot.monitoring.controller;

import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import com.magicworld.tfg_angular_springboot.monitoring.dto.DashboardSnapshot;
import com.magicworld.tfg_angular_springboot.monitoring.dto.EventRequest;
import com.magicworld.tfg_angular_springboot.monitoring.event.ParkEvent;
import com.magicworld.tfg_angular_springboot.monitoring.service.DashboardService;
import com.magicworld.tfg_angular_springboot.monitoring.service.EventIngestionService;
import com.magicworld.tfg_angular_springboot.monitoring.service.MonitoringWebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "Park monitoring and dashboard endpoints")
public class MonitoringController {

    private final DashboardService dashboardService;
    private final EventIngestionService eventService;
    private final MonitoringWebSocketService webSocketService;
    private final DailyOperationsService dailyOperationsService;

    @Operation(summary = "Get dashboard snapshot")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSnapshot> getDashboard() {
        dailyOperationsService.initializeDay(LocalDate.now());
        DashboardSnapshot snapshot = dashboardService.getSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    @Operation(summary = "Record a park event")
    @PostMapping("/events")
    public ResponseEntity<ParkEvent> recordEvent(@RequestBody @Valid EventRequest request) {
        ParkEvent event = eventService.recordEvent(request);
        dashboardService.updateAttractionState(
                request.getAttractionId(), request.getEventType(), request.getQueueSize());
        webSocketService.broadcastDashboard(dashboardService.getSnapshot());
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Get recent events")
    @GetMapping("/events")
    public ResponseEntity<List<ParkEvent>> getRecentEvents(
            @RequestParam(defaultValue = "30") int minutes) {
        return ResponseEntity.ok(eventService.getRecentEvents(minutes));
    }
}
