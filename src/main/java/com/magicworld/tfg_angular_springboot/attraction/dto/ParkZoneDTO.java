package com.magicworld.tfg_angular_springboot.attraction.dto;

import com.magicworld.tfg_angular_springboot.attraction.ParkZoneName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkZoneDTO {
    private Long id;
    private ParkZoneName zoneName;
    private String description;
    private List<ZoneAttractionInfo> attractions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneAttractionInfo {
        private Long id;
        private String name;
        private boolean active;
    }
}

