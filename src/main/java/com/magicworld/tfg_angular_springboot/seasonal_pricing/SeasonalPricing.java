package com.magicworld.tfg_angular_springboot.seasonal_pricing;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seasonal_pricing")
public class SeasonalPricing extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @DecimalMin(value = "1.00")
    @Column(name = "multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier;

    @NotNull
    @Column(name = "apply_on_weekdays", nullable = false)
    private Boolean applyOnWeekdays;

    @NotNull
    @Column(name = "apply_on_weekends", nullable = false)
    private Boolean applyOnWeekends;
}

