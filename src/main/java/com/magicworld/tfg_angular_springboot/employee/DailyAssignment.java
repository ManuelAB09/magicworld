package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.ParkZone;
import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "daily_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employee_id", "assignment_date" })
})
public class DailyAssignment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private DailyStatus currentStatus = DailyStatus.NOT_STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_zone_id")
    private ParkZone currentZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_attraction_id")
    private Attraction currentAttraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_alert_id")
    private ParkAlert assignedAlert;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "break_group", nullable = false)
    @Builder.Default
    private BreakGroup breakGroup = BreakGroup.A;

    @NotNull
    @Column(name = "is_overtime", nullable = false)
    @Builder.Default
    private Boolean isOvertime = false;

    public boolean isAvailable() {
        return currentStatus == DailyStatus.WORKING;
    }
}
