package com.magicworld.tfg_angular_springboot.discount;

import com.magicworld.tfg_angular_springboot.util.BaseEntity;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount")
public class Discount extends BaseEntity {

    @NotNull
    @Min(1)
    @Max(100)
    @Column(name = "discount_percentage", nullable = false)
    private Integer discountPercentage;

    @NotNull
    @Future(message = "error.discount.expiryDate.past")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotBlank
    @Column(name = "discount_code", nullable = false, length = 20)
    private String discountCode;
}
