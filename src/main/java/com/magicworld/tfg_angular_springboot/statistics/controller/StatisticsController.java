package com.magicworld.tfg_angular_springboot.statistics.controller;

import com.magicworld.tfg_angular_springboot.statistics.dto.*;
import com.magicworld.tfg_angular_springboot.statistics.service.EmployeeStatsService;
import com.magicworld.tfg_angular_springboot.statistics.service.ParkStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Employee and park metrics dashboard")
public class StatisticsController {

    private final EmployeeStatsService employeeStatsService;
    private final ParkStatsService parkStatsService;

    // ───── Employee Metrics ─────

    @GetMapping("/employees/hours-ranking")
    @Operation(summary = "Ranking of employees by total hours worked")
    public ResponseEntity<List<EmployeeHoursRankingDTO>> getHoursRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(employeeStatsService.getHoursRanking(from, to));
    }

    @GetMapping("/employees/absence-ranking")
    @Operation(summary = "Ranking of employees by absence count")
    public ResponseEntity<List<EmployeeAbsenceRankingDTO>> getAbsenceRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(employeeStatsService.getAbsenceRanking(from, to));
    }

    @GetMapping("/employees/position-frequency/{employeeId}")
    @Operation(summary = "Frequency of assignment to positions/zones for an employee")
    public ResponseEntity<List<PositionFrequencyDTO>> getPositionFrequency(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(employeeStatsService.getPositionFrequency(employeeId, from, to));
    }

    @GetMapping("/employees/salary")
    @Operation(summary = "Salary calculation report with currency adaptation")
    public ResponseEntity<List<SalaryReportDTO>> getSalaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "es") String locale) {
        return ResponseEntity.ok(employeeStatsService.getSalaryReport(from, to, locale));
    }

    // ───── Park Metrics ─────

    @GetMapping("/park/ticket-sales")
    @Operation(summary = "Total ticket sales and revenue for a date range")
    public ResponseEntity<TicketSalesDTO> getTicketSales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "es") String locale) {
        return ResponseEntity.ok(parkStatsService.getTicketSales(from, to, locale));
    }

    @GetMapping("/park/seasonality")
    @Operation(summary = "Monthly sales breakdown for a given year (seasonality)")
    public ResponseEntity<List<MonthlySalesDTO>> getSeasonality(
            @RequestParam int year,
            @RequestParam(defaultValue = "es") String locale) {
        return ResponseEntity.ok(parkStatsService.getSeasonalBreakdown(year, locale));
    }

    @GetMapping("/park/attraction-performance")
    @Operation(summary = "Attraction performance ranked by queue events (from simulator data)")
    public ResponseEntity<List<AttractionPerformanceDTO>> getAttractionPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(parkStatsService.getAttractionPerformance(from, to));
    }
}
