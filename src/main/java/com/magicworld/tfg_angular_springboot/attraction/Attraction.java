package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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

}
