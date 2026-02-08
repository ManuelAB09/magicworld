package com.magicworld.tfg_angular_springboot.monitoring.metrics;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "park_metrics", indexes = {
    @Index(name = "idx_park_metrics_timestamp", columnList = "timestamp"),
    @Index(name = "idx_park_metrics_attraction", columnList = "attraction_id")
})
public class ParkMetrics extends BaseEntity {

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "attraction_id")
    private Long attractionId;

    @Column(name = "current_visitors")
    private Integer currentVisitors;

    @Column(name = "queue_size")
    private Integer queueSize;

    @Column(name = "avg_wait_time_minutes")
    private Integer avgWaitTimeMinutes;

    @Column(name = "rides_completed")
    private Integer ridesCompleted;

    @Column(name = "total_entries_today")
    private Integer totalEntriesToday;

    @Column(name = "total_sales_today")
    private Integer totalSalesToday;
}
