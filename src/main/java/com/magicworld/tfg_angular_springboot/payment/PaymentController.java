package com.magicworld.tfg_angular_springboot.payment;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Operation(summary = "Get Stripe public key", description = "Returns the Stripe publishable key for frontend", tags = {"Payment"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stripe key returned", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/stripe-key")
    public ResponseEntity<Map<String, String>> getStripePublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", stripePublicKey));
    }

    @Operation(summary = "Get ticket availability", description = "Returns available tickets for a specific date", tags = {"Payment"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability returned", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/availability")
    public ResponseEntity<List<TicketAvailabilityDTO>> getAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(paymentService.getAvailability(date));
    }

    @Getter
    @Setter
    public static class PriceCalculationRequest {
        private List<PaymentRequest.PaymentLineItem> items;
        private List<String> discountCodes;
    }

    @Operation(summary = "Calculate price", description = "Calculate total price with discounts", tags = {"Payment"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Price calculated", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/calculate")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(@RequestBody PriceCalculationRequest request) {
        return ResponseEntity.ok(paymentService.calculatePrice(request.getItems(), request.getDiscountCodes()));
    }

    @Operation(summary = "Process payment", description = "Process payment with Stripe and create purchase", tags = {"Payment"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processed", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid payment data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Payment failed", content = @Content)
    })
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody @Valid PaymentRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
        try {
            PaymentResponse response = paymentService.processPayment(request, lang);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(PaymentResponse.builder()
                    .success(false)
                    .message("error.payment.stripe.failed")
                    .build());
        }
    }
}

