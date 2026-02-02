package com.magicworld.tfg_angular_springboot.purchase;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDTO {
    private Long id;
    private LocalDate purchaseDate;
    private List<PurchaseLineDTO> lines;
}
