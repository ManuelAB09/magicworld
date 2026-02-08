package com.magicworld.tfg_angular_springboot.monitoring.event;

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
@Table(name = "park_event", indexes = {
    @Index(name = "idx_park_event_timestamp", columnList = "timestamp"),
    @Index(name = "idx_park_event_type", columnList = "event_type"),
    @Index(name = "idx_park_event_attraction", columnList = "attraction_id")
})
public class ParkEvent extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private ParkEventType eventType;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "attraction_id")
    private Long attractionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "visitor_count")
    private Integer visitorCount;

    @Column(name = "queue_size")
    private Integer queueSize;

    @Column(name = "metadata", length = 500)
    private String metadata;
}
