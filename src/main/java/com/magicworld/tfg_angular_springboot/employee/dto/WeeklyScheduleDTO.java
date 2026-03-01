package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.BreakGroup;
import com.magicworld.tfg_angular_springboot.employee.WorkShift;
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
public class WeeklyScheduleDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate weekStartDate;
    private DayOfWeek dayOfWeek;
    private WorkShift shift;
    private Long assignedZoneId;
    private String assignedZoneName;
    private Long assignedAttractionId;
    private String assignedAttractionName;
    private BreakGroup breakGroup;
    private Boolean isOvertime;
}
