package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionOption {
    private String id;
    private String label;
    private String description;
    private boolean enabled;
}
