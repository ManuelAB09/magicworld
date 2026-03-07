package com.magicworld.tfg_angular_springboot.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHoursSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private String role;
    private BigDecimal scheduledHours;
    private BigDecimal normalHoursWorked;
    private BigDecimal overtimeHours;
    private BigDecimal totalHoursWorked;
    private int absences;
    private int scheduledDays;
    private int workedDays;
    private List<WorkLogEntryDTO> adjustments;
}

