package com.magicworld.tfg_angular_springboot.employee.dto;

import com.magicworld.tfg_angular_springboot.employee.WorkLogAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogEntryRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate targetDate;

    @NotNull
    private WorkLogAction action;

    @NotNull
    private BigDecimal hoursAffected;

    private Boolean isOvertime;

    @NotNull
    @Size(min = 1, max = 255)
    private String reason;
}

