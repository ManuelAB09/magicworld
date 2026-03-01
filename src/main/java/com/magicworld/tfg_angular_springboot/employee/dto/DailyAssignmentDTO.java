package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.DailyStatus;
import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAssignmentDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private EmployeeRole employeeRole;
    private LocalDate assignmentDate;
    private DailyStatus currentStatus;
    private Long currentZoneId;
    private String currentZoneName;
    private Long currentAttractionId;
    private String currentAttractionName;
    private Long assignedAlertId;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
}

