package com.magicworld.tfg_angular_springboot.ticket_type;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_type")
public class TicketType extends BaseEntity {

    @NotNull
    @Positive
    @Column(name = "cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @NotBlank
    @Column(name = "currency")
    private String currency;

    @NotBlank
    @Size(max = 50)
    @Column(name = "type_name", nullable = false, length = 50, unique = true)
    private String typeName;

    @NotBlank
    @Size(max = 255)
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Positive
    @Column(name = "max_per_day", nullable = false)
    private Integer maxPerDay;

    @NotNull
    @Column(name="photo_url",nullable = false)
    private String photoUrl;
}
