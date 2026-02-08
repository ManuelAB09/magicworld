package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionResult {
    private boolean success;
    private String message;
    private String impact;
    private int satisfactionChange;
    private int waitTimeChange;
    private int costIncurred;
}
