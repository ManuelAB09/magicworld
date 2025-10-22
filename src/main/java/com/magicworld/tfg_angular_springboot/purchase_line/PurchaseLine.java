package com.magicworld.tfg_angular_springboot.purchase_line;

import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_line")
public class PurchaseLine extends BaseEntity {

    @NotNull
    @Column(name = "valid_date", nullable = false)
    private LocalDate validDate;

    @NotNull
    @Positive
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    @NotNull
    @Positive
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @NotNull
    @Size(max = 50)
    @Column(name = "ticket_type_name", nullable = false, length = 50)
    private String ticketTypeName;
}
