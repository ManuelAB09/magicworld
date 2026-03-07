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
public class MonthlySalesDTO {
    private int month;
    private String monthName;
    private int ticketsSold;
    private BigDecimal revenue;
}

