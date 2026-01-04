package com.magicworld.tfg_angular_springboot.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "error.payment.visitDate.required")
    private LocalDate visitDate;

    @NotEmpty(message = "error.payment.items.required")
    @Valid
    private List<PaymentLineItem> items;

    private List<String> discountCodes;

    @NotBlank(message = "error.payment.email.required")
    @Email(message = "error.payment.email.invalid")
    private String email;

    @NotBlank(message = "error.payment.firstName.required")
    private String firstName;

    @NotBlank(message = "error.payment.lastName.required")
    private String lastName;

    private String stripePaymentMethodId;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentLineItem {
        @NotBlank(message = "error.payment.item.typeName.required")
        private String ticketTypeName;

        @NotNull(message = "error.payment.item.quantity.required")
        @Min(value = 1, message = "error.payment.item.quantity.min")
        private Integer quantity;
    }
}

