package com.magicworld.tfg_angular_springboot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryReportDTO {
    private Long employeeId;
    private String fullName;
    private String role;
    private BigDecimal normalHours;
    private BigDecimal overtimeHours;
    private BigDecimal hourlyRate;
    private BigDecimal overtimeRate;
    private BigDecimal normalPay;
    private BigDecimal overtimePay;
    private BigDecimal totalSalary;
    private String currency;
}

