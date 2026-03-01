package com.magicworld.tfg_angular_springboot.employee.controller;

import com.magicworld.tfg_angular_springboot.employee.dto.*;
import com.magicworld.tfg_angular_springboot.employee.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Weekly schedule management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/week")
    @Operation(summary = "Get schedule for a specific week")
    public ResponseEntity<List<WeeklyScheduleDTO>> getWeekSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(scheduleService.getWeekSchedule(weekStart));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get schedule for a specific employee")
    public ResponseEntity<List<WeeklyScheduleDTO>> getEmployeeSchedule(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(scheduleService.getEmployeeSchedule(employeeId, weekStart));
    }

    @PostMapping
    @Operation(summary = "Create a schedule entry")
    public ResponseEntity<WeeklyScheduleDTO> createScheduleEntry(
            @Valid @RequestBody CreateScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.createScheduleEntry(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a schedule entry")
    public ResponseEntity<Void> deleteScheduleEntry(@PathVariable Long id) {
        scheduleService.deleteScheduleEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/copy-week")
    @Operation(summary = "Copy previous week schedule to target week")
    public ResponseEntity<Void> copyPreviousWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetWeekStart) {
        scheduleService.copyPreviousWeek(targetWeekStart);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate coverage for a week")
    public ResponseEntity<CoverageValidationResult> validateWeekCoverage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(scheduleService.validateWeekCoverage(weekStart));
    }

    @PostMapping("/auto-assign")
    @Operation(summary = "Auto-assign employees to a week based on roles and attractions")
    public ResponseEntity<Void> autoAssignWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        scheduleService.autoAssignWeek(weekStart);
        return ResponseEntity.ok().build();
    }
}

