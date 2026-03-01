package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.ParkZone;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weekly_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "employee_id", "week_start_date", "day_of_week" })
})
public class WeeklySchedule extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "shift", nullable = false)
    private WorkShift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private ParkZone assignedZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id")
    private Attraction assignedAttraction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "break_group", nullable = false)
    @Builder.Default
    private BreakGroup breakGroup = BreakGroup.A;

    @Column(name = "is_overtime")
    @Builder.Default
    private Boolean isOvertime = false;

    public LocalDate getActualDate() {
        return weekStartDate.plusDays(dayOfWeek.getValue() - 1);
    }
}
