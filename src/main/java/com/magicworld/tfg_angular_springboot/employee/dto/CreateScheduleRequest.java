package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.BreakGroup;
import com.magicworld.tfg_angular_springboot.employee.WorkShift;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate weekStartDate;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private WorkShift shift;

    private Long assignedZoneId;

    private Long assignedAttractionId;

    @NotNull
    private BreakGroup breakGroup;
}

