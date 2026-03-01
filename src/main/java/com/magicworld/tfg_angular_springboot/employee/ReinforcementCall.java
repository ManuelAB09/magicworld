package com.magicworld.tfg_angular_springboot.employee;

import com.magicworld.tfg_angular_springboot.monitoring.alert.ParkAlert;
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
@Table(name = "reinforcement_call")
public class ReinforcementCall extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "call_time", nullable = false)
    private LocalDateTime callTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private ParkAlert originAlert;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReinforcementStatus status = ReinforcementStatus.PENDING;

    @Column(name = "response_time")
    private LocalDateTime responseTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @NotNull
    @Column(name = "is_overtime", nullable = false)
    @Builder.Default
    private Boolean isOvertime = false;
}
