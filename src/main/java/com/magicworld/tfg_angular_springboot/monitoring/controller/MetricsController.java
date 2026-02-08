package com.magicworld.tfg_angular_springboot.monitoring.controller;

import com.magicworld.tfg_angular_springboot.monitoring.dto.MetricsExportRow;
import com.magicworld.tfg_angular_springboot.monitoring.dto.MetricsHistory;
import com.magicworld.tfg_angular_springboot.monitoring.dto.MetricsPoint;
import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetrics;
import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetricsRepository;
import com.magicworld.tfg_angular_springboot.monitoring.service.MetricsExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monitoring/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Historical metrics and export")
public class MetricsController {

    private final ParkMetricsRepository metricsRepository;
    private final MetricsExportService exportService;

    @Operation(summary = "Get metrics history for an attraction")
    @GetMapping("/attraction/{attractionId}")
    public ResponseEntity<MetricsHistory> getAttractionMetrics(
            @PathVariable Long attractionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<ParkMetrics> metrics = metricsRepository
                .findByAttractionIdAndTimestampBetweenOrderByTimestampAsc(attractionId, start, end);

        List<MetricsPoint> points = metrics.stream()
                .map(this::toMetricsPoint)
                .toList();

        return ResponseEntity.ok(MetricsHistory.builder()
                .startTime(start)
                .endTime(end)
                .dataPoints(points)
                .build());
    }

    @Operation(summary = "Get global park metrics history")
    @GetMapping("/global")
    public ResponseEntity<MetricsHistory> getGlobalMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<ParkMetrics> metrics = metricsRepository.findGlobalMetricsBetween(start, end);

        List<MetricsPoint> globalMetrics = metrics.stream()
                .map(this::toMetricsPoint)
                .toList();

        return ResponseEntity.ok(MetricsHistory.builder()
                .startTime(start)
                .endTime(end)
                .dataPoints(globalMetrics)
                .build());
    }

    @Operation(summary = "Export metrics to CSV")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<MetricsExportRow> rows = exportService.getMetricsForExport(start, end);
        byte[] csv = exportService.generateCsv(rows);

        String filename = "metrics_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private MetricsPoint toMetricsPoint(ParkMetrics m) {
        return MetricsPoint.builder()
                .timestamp(m.getTimestamp())
                .visitors(m.getCurrentVisitors() != null ? m.getCurrentVisitors() : 0)
                .queueSize(m.getQueueSize() != null ? m.getQueueSize() : 0)
                .waitTimeMinutes(m.getAvgWaitTimeMinutes() != null ? m.getAvgWaitTimeMinutes() : 0)
                .build();
    }
}
