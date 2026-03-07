package com.magicworld.tfg_angular_springboot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAbsenceRankingDTO {
    private Long employeeId;
    private String fullName;
    private String role;
    private int absenceCount;
    private int scheduledDays;
}

