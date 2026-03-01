package com.magicworld.tfg_angular_springboot.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolutionResult {
    private boolean success;
    private String message;
    private String code;
    private Object[] args;
    private String actionTaken;
    private Map<String, Object> resourcesUsed;
    private String failureReason;
}
