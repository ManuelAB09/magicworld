package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SeasonalPricingRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal multiplier;

    @NotNull
    private Boolean applyOnWeekdays;

    @NotNull
    private Boolean applyOnWeekends;
}

