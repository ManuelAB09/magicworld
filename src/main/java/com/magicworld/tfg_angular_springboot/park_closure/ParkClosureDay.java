package com.magicworld.tfg_angular_springboot.park_closure;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "park_closure_day")
public class ParkClosureDay extends BaseEntity {

    @NotNull
    @Column(name = "closure_date", nullable = false, unique = true)
    private LocalDate closureDate;

    @NotBlank
    @Size(max = 255)
    @Column(name = "reason", nullable = false)
    private String reason;
}

