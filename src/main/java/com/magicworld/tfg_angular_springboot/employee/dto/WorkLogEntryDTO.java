package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.WorkLogAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogEntryDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate targetDate;
    private WorkLogAction action;
    private BigDecimal hoursAffected;
    private Boolean isOvertime;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;
}

