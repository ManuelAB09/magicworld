package com.magicworld.tfg_angular_springboot.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private List<String> validDiscountCodes;
    private List<String> invalidDiscountCodes;
    private List<String> validButNotApplicableCodes;
    private Map<String, Integer> discountPercentages;
    private Map<String, List<String>> discountAppliesTo;
}

