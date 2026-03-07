package com.magicworld.tfg_angular_springboot.park_closure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ParkClosureDayRequest {

    @NotNull
    private LocalDate closureDate;

    @NotBlank
    @Size(max = 255)
    private String reason;
}

