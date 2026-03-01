package com.magicworld.tfg_angular_springboot.employee.controller;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import com.magicworld.tfg_angular_springboot.employee.ReinforcementCall;
import com.magicworld.tfg_angular_springboot.employee.ReinforcementStatus;
import com.magicworld.tfg_angular_springboot.employee.dto.AvailableEmployeesResponse;
import com.magicworld.tfg_angular_springboot.employee.dto.DailyAssignmentDTO;
import com.magicworld.tfg_angular_springboot.employee.service.DailyOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-operations")
@RequiredArgsConstructor
@Tag(name = "Daily Operations", description = "Real-time daily staff operations")
@PreAuthorize("hasRole('ADMIN')")
public class DailyOperationsController {

    private final DailyOperationsService dailyOperationsService;

    @PostMapping("/initialize")
    @Operation(summary = "Initialize assignments for a specific date")
    public ResponseEntity<Void> initializeDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dailyOperationsService.initializeDay(date);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's assignments")
    public ResponseEntity<List<DailyAssignmentDTO>> getTodayAssignments() {
        return ResponseEntity.ok(dailyOperationsService.getTodayAssignments());
    }

    @GetMapping("/date")
    @Operation(summary = "Get assignments for a specific date")
    public ResponseEntity<List<DailyAssignmentDTO>> getAssignmentsForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dailyOperationsService.getAssignmentsForDate(date));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available employees by role")
    public ResponseEntity<AvailableEmployeesResponse> getAvailableEmployees(
            @RequestParam EmployeeRole role) {
        return ResponseEntity.ok(dailyOperationsService.getAvailableEmployees(role));
    }

    @PostMapping("/assign-to-alert")
    @Operation(summary = "Assign an employee to handle an alert")
    public ResponseEntity<DailyAssignmentDTO> assignEmployeeToAlert(
            @RequestParam Long employeeId,
            @RequestParam Long alertId) {
        return ResponseEntity.ok(dailyOperationsService.assignEmployeeToAlert(employeeId, alertId));
    }

    @PostMapping("/release-from-alert")
    @Operation(summary = "Release an employee from alert duty")
    public ResponseEntity<DailyAssignmentDTO> releaseEmployeeFromAlert(
            @RequestParam Long employeeId) {
        return ResponseEntity.ok(dailyOperationsService.releaseEmployeeFromAlert(employeeId));
    }

    @PostMapping("/call-reinforcement")
    @Operation(summary = "Call a reinforcement employee")
    public ResponseEntity<ReinforcementCall> callReinforcement(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Long alertId) {
        return ResponseEntity.ok(dailyOperationsService.callReinforcement(employeeId, alertId));
    }

    @PostMapping("/reinforcement/{callId}/status")
    @Operation(summary = "Update reinforcement call status")
    public ResponseEntity<ReinforcementCall> updateReinforcementStatus(
            @PathVariable Long callId,
            @RequestParam ReinforcementStatus status) {
        return ResponseEntity.ok(dailyOperationsService.updateReinforcementStatus(callId, status));
    }
}

