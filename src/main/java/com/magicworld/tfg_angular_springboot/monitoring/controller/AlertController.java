package com.magicworld.tfg_angular_springboot.monitoring.controller;

import com.magicworld.tfg_angular_springboot.monitoring.dto.AlertDTO;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolveAlertRequest;
import com.magicworld.tfg_angular_springboot.monitoring.dto.ResolutionResult;
import com.magicworld.tfg_angular_springboot.monitoring.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/monitoring/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Park alerts management")
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "Get active alerts")
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @Operation(summary = "Resolve an alert with chosen option")
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ResolutionResult> resolveAlert(
            @PathVariable Long id,
            @RequestBody ResolveAlertRequest request) {
        ResolutionResult result = alertService.resolveAlert(id, request);
        return ResponseEntity.ok(result);
    }
}
