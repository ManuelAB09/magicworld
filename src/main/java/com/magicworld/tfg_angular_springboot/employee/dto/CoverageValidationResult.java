package com.magicworld.tfg_angular_springboot.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageValidationResult {
    private boolean valid;
    private LocalDate weekStartDate;
    private List<CoverageIssue> issues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoverageIssue {
        private LocalDate date;
        private String issueType;
        private String description;
        private Long attractionId;
        private String attractionName;
        private Long zoneId;
        private String zoneName;
    }
}

