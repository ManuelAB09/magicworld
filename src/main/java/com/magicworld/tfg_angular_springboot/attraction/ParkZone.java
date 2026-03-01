package com.magicworld.tfg_angular_springboot.attraction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "park_zone")
public class ParkZone extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "zone_name", nullable = false, unique = true)
    private ParkZoneName zoneName;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "zone")
    @Builder.Default
    private List<Attraction> attractions = new ArrayList<>();
}

