package com.magicworld.tfg_angular_springboot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionFrequencyDTO {
    private String positionName;
    private String positionType;
    private int assignmentCount;
}

