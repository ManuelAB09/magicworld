package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.ParkZone;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_log", indexes = {
    @Index(name = "idx_work_log_employee", columnList = "employee_id"),
    @Index(name = "idx_work_log_date", columnList = "target_date")
})
public class WorkLog extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private WorkLogAction action;

    @NotNull
    @Column(name = "hours_affected", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal hoursAffected = BigDecimal.ZERO;

    @NotNull
    @Column(name = "is_overtime", nullable = false)
    @Builder.Default
    private Boolean isOvertime = false;

    @NotNull
    @Size(max = 255)
    @Column(name = "reason", nullable = false)
    private String reason;

    @NotNull
    @Size(max = 100)
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Snapshot of the schedule entry removed (for restoration on REMOVE_ABSENCE) ──

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_attraction_id")
    private Attraction snapshotAttraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_zone_id")
    private ParkZone snapshotZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_shift")
    private WorkShift snapshotShift;

    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_break_group")
    private BreakGroup snapshotBreakGroup;
}

