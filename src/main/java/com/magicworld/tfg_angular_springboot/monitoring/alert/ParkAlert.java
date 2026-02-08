package com.magicworld.tfg_angular_springboot.monitoring.alert;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "park_alert", indexes = {
    @Index(name = "idx_park_alert_timestamp", columnList = "timestamp"),
    @Index(name = "idx_park_alert_active", columnList = "is_active")
})
public class ParkAlert extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    @NotNull
    @Size(max = 255)
    @Column(name = "message", nullable = false)
    private String message;

    @Size(max = 255)
    @Column(name = "suggestion")
    private String suggestion;

    @Column(name = "attraction_id")
    private Long attractionId;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
