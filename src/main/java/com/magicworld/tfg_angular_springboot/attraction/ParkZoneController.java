package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.attraction.dto.ParkZoneDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Tag(name = "Park Zones", description = "Park zone information endpoints")
public class ParkZoneController {

    private final ParkZoneRepository zoneRepository;
    private final AttractionRepository attractionRepository;

    @GetMapping
    @Operation(summary = "Get all park zones")
    public ResponseEntity<List<ParkZoneDTO>> getAllZones() {
        List<ParkZoneDTO> zones = zoneRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get zone by ID")
    public ResponseEntity<ParkZoneDTO> getZone(@PathVariable Long id) {
        return zoneRepository.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ParkZoneDTO toDTO(ParkZone zone) {
        List<ParkZoneDTO.ZoneAttractionInfo> attractions = attractionRepository.findByZoneId(zone.getId())
                .stream()
                .map(a -> ParkZoneDTO.ZoneAttractionInfo.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .active(a.getIsActive())
                        .build())
                .toList();

        return ParkZoneDTO.builder()
                .id(zone.getId())
                .zoneName(zone.getZoneName())
                .description(zone.getDescription())
                .attractions(attractions)
                .build();
    }
}

