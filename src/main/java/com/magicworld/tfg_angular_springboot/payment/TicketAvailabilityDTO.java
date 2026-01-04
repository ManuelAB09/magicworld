package com.magicworld.tfg_angular_springboot.payment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAvailabilityDTO {
    private Long id;
    private String typeName;
    private String description;
    private BigDecimal cost;
    private String currency;
    private String photoUrl;
    private Integer maxPerDay;
    private Integer available;
}

