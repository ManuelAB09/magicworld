package com.magicworld.tfg_angular_springboot.employee.controller;

import com.magicworld.tfg_angular_springboot.employee.dto.EmployeeHoursSummaryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryDTO;
import com.magicworld.tfg_angular_springboot.employee.dto.WorkLogEntryRequest;
import com.magicworld.tfg_angular_springboot.employee.service.WorkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/worklog")
@RequiredArgsConstructor
@Tag(name = "Work Log", description = "Employee hours, absences and audit log management")
public class WorkLogController {

    private final WorkLogService workLogService;

    @GetMapping("/summary/{employeeId}")
    @Operation(summary = "Get hours/absences summary for an employee in a date range")
    public ResponseEntity<EmployeeHoursSummaryDTO> getEmployeeSummary(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(workLogService.getEmployeeSummary(employeeId, from, to));
    }

    @GetMapping("/history/{employeeId}")
    @Operation(summary = "Get audit log history for an employee")
    public ResponseEntity<List<WorkLogEntryDTO>> getWorkLogHistory(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(workLogService.getWorkLogHistory(employeeId, from, to));
    }

    @PostMapping("/entry")
    @Operation(summary = "Add a work log adjustment entry (admin action)")
    public ResponseEntity<WorkLogEntryDTO> addWorkLogEntry(
            @Valid @RequestBody WorkLogEntryRequest request,
            Authentication authentication) {
        String adminUsername = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workLogService.addWorkLogEntry(request, adminUsername));
    }
}
