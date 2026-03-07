package com.magicworld.tfg_angular_springboot.payment;

import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountService;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.email.EmailService;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.park_closure.ParkClosureDayService;
import com.magicworld.tfg_angular_springboot.purchase.Purchase;
import com.magicworld.tfg_angular_springboot.purchase.PurchaseService;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLine;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineService;
import com.magicworld.tfg_angular_springboot.qr.QrCodeService;
import com.magicworld.tfg_angular_springboot.seasonal_pricing.SeasonalPricingService;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${park.max-capacity:500}")
    private int parkMaxCapacity;

    private final TicketTypeService ticketTypeService;
    private final PurchaseLineService purchaseLineService;
    private final PurchaseService purchaseService;
    private final DiscountService discountService;
    private final DiscountTicketTypeService discountTicketTypeService;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SeasonalPricingService seasonalPricingService;
    private final ParkClosureDayService parkClosureDayService;

    public List<TicketAvailabilityDTO> getAvailability(LocalDate date) {
        if (parkClosureDayService.isClosedDay(date)) {
            throw new InvalidOperationException("error.payment.park.closed");
        }

        BigDecimal multiplier = seasonalPricingService.getMultiplier(date);
        List<TicketType> ticketTypes = ticketTypeService.findAll();

        return ticketTypes.stream()
                .map(tt -> {
                    int available = purchaseLineService.getAvailableQuantity(tt.getTypeName(), date);
                    BigDecimal adjustedCost = tt.getCost().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
                    return TicketAvailabilityDTO.builder()
                            .id(tt.getId())
                            .typeName(tt.getTypeName())
                            .description(tt.getDescription())
                            .cost(tt.getCost())
                            .adjustedCost(adjustedCost)
                            .seasonalMultiplier(multiplier)
                            .photoUrl(tt.getPhotoUrl())
                            .maxPerDay(tt.getMaxPerDay())
                            .available(available)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PriceCalculationResponse calculatePrice(List<PaymentRequest.PaymentLineItem> items,
            List<String> discountCodes, LocalDate visitDate) {
        BigDecimal multiplier = visitDate != null ? seasonalPricingService.getMultiplier(visitDate) : BigDecimal.ONE;
        BigDecimal subtotal = BigDecimal.ZERO;
        Map<String, BigDecimal> itemSubtotals = new HashMap<>();
        Set<String> itemTicketTypes = new HashSet<>();

        for (PaymentRequest.PaymentLineItem item : items) {
            TicketType ticketType = ticketTypeService.findByTypeName(item.getTicketTypeName());
            BigDecimal adjustedCost = ticketType.getCost().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = adjustedCost.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            itemSubtotals.put(item.getTicketTypeName(), lineTotal);
            itemTicketTypes.add(item.getTicketTypeName());
        }

        List<String> validCodes = new ArrayList<>();
        List<String> invalidCodes = new ArrayList<>();
        List<String> validButNotApplicableCodes = new ArrayList<>();
        Map<String, Integer> discountPercentages = new HashMap<>();
        Map<String, Discount> validDiscountsMap = new HashMap<>();
        Map<String, List<String>> discountAppliesTo = new HashMap<>();

        if (discountCodes != null) {
            for (String code : discountCodes) {
                String trimmedCode = code.trim();
                if (trimmedCode.isEmpty())
                    continue;

                Discount discount = discountService.findByCode(trimmedCode);
                if (discount != null && !discount.getExpiryDate().isBefore(LocalDate.now())) {
                    // Check which items this discount applies to
                    List<String> applicableItems = new ArrayList<>();
                    for (String ticketTypeName : itemTicketTypes) {
                        if (discountTicketTypeService.discountAppliesToTicketType(discount.getId(), ticketTypeName)) {
                            applicableItems.add(ticketTypeName);
                        }
                    }

                    if (applicableItems.isEmpty()) {
                        // Valid discount but no matching items in cart
                        validButNotApplicableCodes.add(trimmedCode);
                        discountPercentages.put(trimmedCode, discount.getDiscountPercentage());
                    } else {
                        validCodes.add(trimmedCode);
                        discountPercentages.put(trimmedCode, discount.getDiscountPercentage());
                        validDiscountsMap.put(trimmedCode, discount);
                        discountAppliesTo.put(trimmedCode, applicableItems);
                    }
                } else {
                    invalidCodes.add(trimmedCode);
                }
            }
        }

        BigDecimal discountAmount = calculateDiscountAmount(items, validDiscountsMap, itemSubtotals);
        BigDecimal total = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        return PriceCalculationResponse.builder()
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP))
                .total(total.setScale(2, RoundingMode.HALF_UP))
                .validDiscountCodes(validCodes)
                .invalidDiscountCodes(invalidCodes)
                .validButNotApplicableCodes(validButNotApplicableCodes)
                .discountPercentages(discountPercentages)
                .discountAppliesTo(discountAppliesTo)
                .build();
    }

    private BigDecimal calculateDiscountAmount(List<PaymentRequest.PaymentLineItem> items,
            Map<String, Discount> validDiscountsMap,
            Map<String, BigDecimal> itemSubtotals) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        Map<String, Integer> bestDiscountPerItem = new HashMap<>();

        for (PaymentRequest.PaymentLineItem item : items) {
            String ticketTypeName = item.getTicketTypeName();
            int bestPercentage = 0;

            for (Map.Entry<String, Discount> entry : validDiscountsMap.entrySet()) {
                Discount discount = entry.getValue();
                if (discountTicketTypeService.discountAppliesToTicketType(discount.getId(), ticketTypeName)) {
                    bestPercentage = Math.max(bestPercentage, discount.getDiscountPercentage());
                }
            }
            bestDiscountPerItem.put(ticketTypeName, bestPercentage);
        }

        for (Map.Entry<String, Integer> entry : bestDiscountPerItem.entrySet()) {
            if (entry.getValue() > 0) {
                BigDecimal itemTotal = itemSubtotals.get(entry.getKey());
                BigDecimal itemDiscount = itemTotal.multiply(BigDecimal.valueOf(entry.getValue()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }

        return totalDiscount;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String lang) throws StripeException {
        validatePurchase(request);

        // Recalculate price at payment time to handle concurrent changes
        PriceCalculationResponse priceCalc = calculatePrice(request.getItems(), request.getDiscountCodes(), request.getVisitDate());

        // Check if any discount codes the user thought were valid are now invalid or
        // not applicable
        if (request.getDiscountCodes() != null && !request.getDiscountCodes().isEmpty()) {
            List<String> problematicCodes = new ArrayList<>();
            problematicCodes.addAll(priceCalc.getInvalidDiscountCodes());
            problematicCodes.addAll(priceCalc.getValidButNotApplicableCodes());

            if (!problematicCodes.isEmpty()) {
                throw new InvalidOperationException("error.payment.discount.changed",
                        String.join(", ", problematicCodes));
            }
        }

        if (priceCalc.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("error.payment.invalid.total");
        }

        long amountInCents = priceCalc.getTotal().multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .setPaymentMethod(request.getStripePaymentMethodId())
                .setConfirm(true)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(
                                        PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            return PaymentResponse.builder()
                    .success(false)
                    .message("error.payment.failed")
                    .stripePaymentIntentId(paymentIntent.getId())
                    .build();
        }

        User buyer = findOrCreateGuestUser(request);
        Purchase purchase = createPurchaseFromRequest(request, buyer, priceCalc);

        sendConfirmationEmail(purchase, request, priceCalc, lang);
        notifyAvailabilityChange(request.getVisitDate());

        return PaymentResponse.builder()
                .success(true)
                .message("success.payment.completed")
                .purchaseId(purchase.getId())
                .stripePaymentIntentId(paymentIntent.getId())
                .totalAmount(priceCalc.getTotal())
                .discountAmount(priceCalc.getDiscountAmount())
                .appliedDiscountCodes(priceCalc.getValidDiscountCodes())
                .build();
    }

    private void validatePurchase(PaymentRequest request) {
        if (request.getVisitDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("error.payment.date.past");
        }

        if (request.getVisitDate().isAfter(LocalDate.now().plusMonths(2))) {
            throw new InvalidOperationException("error.payment.date.too.far");
        }

        if (parkClosureDayService.isClosedDay(request.getVisitDate())) {
            throw new InvalidOperationException("error.payment.park.closed");
        }

        // Per-ticket-type availability check
        for (PaymentRequest.PaymentLineItem item : request.getItems()) {
            int available = purchaseLineService.getAvailableQuantity(item.getTicketTypeName(), request.getVisitDate());
            if (item.getQuantity() > available) {
                throw new InvalidOperationException("error.payment.insufficient.availability");
            }
        }

        // Global park capacity check
        int totalRequestedQuantity = request.getItems().stream()
                .mapToInt(PaymentRequest.PaymentLineItem::getQuantity).sum();
        int totalSoldForDay = purchaseLineService.getTotalSoldForDate(request.getVisitDate());
        if (totalSoldForDay + totalRequestedQuantity > parkMaxCapacity) {
            throw new InvalidOperationException("error.payment.park.capacity.exceeded");
        }
    }

    private User findOrCreateGuestUser(PaymentRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        String guestUsername = "guest_" + System.currentTimeMillis();
        User guestUser = User.builder()
                .username(guestUsername)
                .firstname(request.getFirstName())
                .lastname(request.getLastName())
                .email(request.getEmail())
                .password(null)
                .userRole(Role.USER)
                .build();

        return userRepository.save(guestUser);
    }

    private Purchase createPurchaseFromRequest(PaymentRequest request, User buyer, PriceCalculationResponse priceCalc) {
        BigDecimal multiplier = seasonalPricingService.getMultiplier(request.getVisitDate());
        List<PurchaseLine> lines = new ArrayList<>();

        for (PaymentRequest.PaymentLineItem item : request.getItems()) {
            TicketType ticketType = ticketTypeService.findByTypeName(item.getTicketTypeName());
            BigDecimal adjustedCost = ticketType.getCost().multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = adjustedCost.multiply(BigDecimal.valueOf(item.getQuantity()));

            PurchaseLine line = PurchaseLine.builder()
                    .validDate(request.getVisitDate())
                    .quantity(item.getQuantity())
                    .totalCost(lineTotal)
                    .ticketTypeName(item.getTicketTypeName())
                    .build();
            lines.add(line);
        }

        return purchaseService.createPurchase(buyer, lines);
    }

    private void sendConfirmationEmail(Purchase purchase, PaymentRequest request,
            PriceCalculationResponse priceCalc, String lang) {
        String qrContent = "MAGICWORLD-TICKET-" + purchase.getId() + "-" + request.getVisitDate();
        byte[] qrCode = qrCodeService.generateQrCodeBytes(qrContent);

        boolean isSpanish = "es".equalsIgnoreCase(lang);
        String currencySymbol = isSpanish ? "€" : "$";
        BigDecimal exchangeRate = isSpanish ? BigDecimal.ONE : new BigDecimal("1.08");

        List<Map<String, Object>> orderLines = new ArrayList<>();
        for (PaymentRequest.PaymentLineItem item : request.getItems()) {
            TicketType tt = ticketTypeService.findByTypeName(item.getTicketTypeName());
            BigDecimal lineTotalEur = tt.getCost().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal lineTotalConverted = lineTotalEur.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> lineMap = new HashMap<>();
            lineMap.put("ticketTypeName", item.getTicketTypeName());
            lineMap.put("quantity", item.getQuantity());
            lineMap.put("totalCost", lineTotalConverted);
            orderLines.add(lineMap);
        }

        BigDecimal discountConverted = priceCalc.getDiscountAmount().multiply(exchangeRate).setScale(2,
                RoundingMode.HALF_UP);
        BigDecimal totalConverted = priceCalc.getTotal().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> vars = new HashMap<>();
        vars.put("subject", isSpanish ? "Confirmación de compra - MagicWorld" : "Purchase Confirmation - MagicWorld");
        vars.put("headerText", isSpanish ? "Confirmación de Compra" : "Purchase Confirmation");
        vars.put("greeting",
                isSpanish ? "¡Hola " + request.getFirstName() + "!" : "Hello " + request.getFirstName() + "!");
        vars.put("thankYouMessage", isSpanish ? "¡Gracias por tu compra! Aquí tienes los detalles de tu pedido."
                : "Thank you for your purchase! Here are your order details.");
        vars.put("visitDateLabel", isSpanish ? "Fecha de visita" : "Visit Date");
        vars.put("visitDate", request.getVisitDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        vars.put("orderSummaryTitle", isSpanish ? "Resumen del Pedido" : "Order Summary");
        vars.put("orderLines", orderLines);
        vars.put("currencySymbol", currencySymbol);
        vars.put("discountApplied", priceCalc.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0);
        vars.put("discountLabel", isSpanish ? "Descuento" : "Discount");
        vars.put("discountAmount", discountConverted);
        vars.put("totalLabel", "Total");
        vars.put("totalAmount", totalConverted);
        vars.put("qrTitle", isSpanish ? "Tu Código QR de Entrada" : "Your Entry QR Code");
        vars.put("qrInstructions", isSpanish ? "Muestra este código QR en la entrada del parque"
                : "Show this QR code at the park entrance");
        vars.put("purchaseId", purchase.getId());
        vars.put("footerMessage",
                isSpanish ? "¡Te esperamos en MagicWorld!" : "We look forward to seeing you at MagicWorld!");
        vars.put("footerRights", isSpanish ? "Todos los derechos reservados" : "All rights reserved");
        vars.put("contactInfo", isSpanish ? "Contáctanos en info@magicworld.com" : "Contact us at info@magicworld.com");

        String subject = isSpanish ? "Confirmación de compra - MagicWorld" : "Purchase Confirmation - MagicWorld";
        emailService.sendHtmlEmailWithQr(request.getEmail(), subject, "purchase-confirmation", vars, qrCode);
    }

    @Transactional
    public void notifyAvailabilityChange(LocalDate date) {
        try {
            List<TicketAvailabilityDTO> availability = getAvailability(date);
            messagingTemplate.convertAndSend("/topic/availability/" + date, availability);
        } catch (InvalidOperationException e) {
            // Park is closed on this date, send empty list
            messagingTemplate.convertAndSend("/topic/availability/" + date, List.of());
        }
    }
}
