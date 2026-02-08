package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionRepository;
import com.magicworld.tfg_angular_springboot.monitoring.dto.MetricsExportRow;
import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetrics;
import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ParkMetricsRepository metricsRepository;
    private final AttractionRepository attractionRepository;

    @Transactional(readOnly = true)
    public List<MetricsExportRow> getMetricsForExport(LocalDateTime start, LocalDateTime end) {
        List<ParkMetrics> metrics = metricsRepository.findByTimestampBetweenOrderByTimestampAsc(start, end);
        Map<Long, Attraction> attractionMap = attractionRepository.findAll().stream()
                .collect(Collectors.toMap(Attraction::getId, Function.identity()));

        return metrics.stream()
                .map(m -> mapToExportRow(m, attractionMap))
                .toList();
    }

    private MetricsExportRow mapToExportRow(ParkMetrics m, Map<Long, Attraction> attractionMap) {
        String attractionName = m.getAttractionId() != null
                ? attractionMap.getOrDefault(m.getAttractionId(), new Attraction()).getName()
                : "Global";

        return MetricsExportRow.builder()
                .timestamp(m.getTimestamp())
                .attractionName(attractionName != null ? attractionName : "Global")
                .queueSize(m.getQueueSize())
                .waitTimeMinutes(m.getAvgWaitTimeMinutes())
                .visitors(m.getCurrentVisitors())
                .ridesCompleted(m.getRidesCompleted())
                .build();
    }

    public byte[] generateCsv(List<MetricsExportRow> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("Timestamp,Attraction,Queue Size,Wait Time (min),Visitors,Rides Completed");

        for (MetricsExportRow row : rows) {
            writer.printf("%s,%s,%s,%s,%s,%s%n",
                    row.getTimestamp().format(DATE_FORMAT),
                    escapeCsv(row.getAttractionName()),
                    valueOrEmpty(row.getQueueSize()),
                    valueOrEmpty(row.getWaitTimeMinutes()),
                    valueOrEmpty(row.getVisitors()),
                    valueOrEmpty(row.getRidesCompleted()));
        }

        writer.flush();
        return out.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }
}
