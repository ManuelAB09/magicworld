package com.magicworld.tfg_angular_springboot.attraction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "attraction")
public class Attraction extends BaseEntity {

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "intensity", nullable = false)
    private Intensity intensity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AttractionCategory category;

    @NotNull
    @Min(0)
    @Column(name = "minimum_height", nullable = false)
    private Integer minimumHeight;

    @NotNull
    @Min(0)
    @Column(name = "minimum_age", nullable = false)
    private Integer minimumAge;

    @NotNull
    @Min(0)
    @Column(name = "minimum_weight", nullable = false)
    private Integer minimumWeight;

    @NotNull
    @Size(max = 255)
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Size(max = 255)
    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "map_position_x", nullable = false)
    private Double mapPositionX;

    @NotNull
    @Column(name = "map_position_y", nullable = false)
    private Double mapPositionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ParkZone zone;

    @NotNull
    @Column(name = "opening_time", nullable = false)
    @Builder.Default
    private LocalTime openingTime = LocalTime.of(9, 0);

    @NotNull
    @Column(name = "closing_time", nullable = false)
    @Builder.Default
    private LocalTime closingTime = LocalTime.of(17, 0);

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_status", nullable = false)
    @Builder.Default
    private MaintenanceStatus maintenanceStatus = MaintenanceStatus.OPERATIONAL;

}
