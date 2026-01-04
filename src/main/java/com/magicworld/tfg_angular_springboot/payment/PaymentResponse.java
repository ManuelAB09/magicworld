package com.magicworld.tfg_angular_springboot.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String message;
    private Long purchaseId;
    private String stripePaymentIntentId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private List<String> appliedDiscountCodes;
}

