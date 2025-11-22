package com.magicworld.tfg_angular_springboot.attraction;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AttractionRequest {
    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private Intensity intensity;

    @NotNull
    @Min(0)
    private Integer minimumHeight;

    @NotNull
    @Min(0)
    private Integer minimumAge;

    @NotNull
    @Min(0)
    private Integer minimumWeight;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    private Boolean isActive;
}

