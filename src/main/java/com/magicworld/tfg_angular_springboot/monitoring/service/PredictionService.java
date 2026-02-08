package com.magicworld.tfg_angular_springboot.monitoring.service;

import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetrics;
import com.magicworld.tfg_angular_springboot.monitoring.metrics.ParkMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final int WINDOW_SIZE = 10;
    private static final double BASE_MINUTES_PER_PERSON = 1.5;
    private static final int RIDE_CAPACITY = 20;
    private static final int RIDE_DURATION_MINUTES = 3;

    private final ParkMetricsRepository metricsRepository;

    @Transactional(readOnly = true)
    public int predictWaitTime(Long attractionId, int currentQueueSize) {
        if (currentQueueSize <= 0) return 0;

        int baseWait = calculateBaseWaitTime(currentQueueSize);

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<ParkMetrics> recent = metricsRepository.findByAttractionIdSince(attractionId, since);

        if (recent.size() < 3) {
            double hourFactor = getHourlyTrendFactor();
            int predictedQueue = Math.max(0, currentQueueSize + (int)(currentQueueSize * hourFactor * 0.15));
            return Math.max(1, calculateBaseWaitTime(predictedQueue));
        }

        double trend = calculateQueueTrend(recent);
        int predictedQueueIn15Min = Math.max(0, currentQueueSize + (int)(trend * 5));
        int predictedWait = calculateBaseWaitTime(predictedQueueIn15Min);

        return Math.max(1, Math.min(predictedWait, 120));
    }

    private double getHourlyTrendFactor() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 10 && hour <= 13) return 0.8;
        if (hour >= 14 && hour <= 17) return 0.5;
        if (hour >= 18 && hour <= 20) return -0.3;
        return 0.0;
    }

    public int calculateBaseWaitTime(int queueSize) {
        if (queueSize <= 0) return 0;
        int cyclesNeeded = (int) Math.ceil((double) queueSize / RIDE_CAPACITY);
        return cyclesNeeded * RIDE_DURATION_MINUTES;
    }

    private double calculateQueueTrend(List<ParkMetrics> metrics) {
        if (metrics.size() < 2) return 0;

        int start = Math.max(0, metrics.size() - WINDOW_SIZE);
        double totalChange = 0;
        int count = 0;

        for (int i = start + 1; i < metrics.size(); i++) {
            Integer prev = metrics.get(i - 1).getQueueSize();
            Integer curr = metrics.get(i).getQueueSize();
            if (prev != null && curr != null) {
                totalChange += (curr - prev);
                count++;
            }
        }

        return count > 0 ? totalChange / count : 0;
    }
}
