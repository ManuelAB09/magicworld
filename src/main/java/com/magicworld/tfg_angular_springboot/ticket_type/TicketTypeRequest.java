package com.magicworld.tfg_angular_springboot.ticket_type;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTypeRequest {
    @NotNull
    @Positive
    private BigDecimal cost;


    @NotBlank
    @Size(max = 50)
    private String typeName;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private Integer maxPerDay;
}