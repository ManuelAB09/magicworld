package com.magicworld.tfg_angular_springboot.purchase;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseLineDTO {
    private Long id;
    private LocalDate validDate;
    private Integer quantity;
    private BigDecimal totalCost;
    private String ticketTypeName;
}
