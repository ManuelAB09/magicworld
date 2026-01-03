package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import com.magicworld.tfg_angular_springboot.discount.Discount;
import com.magicworld.tfg_angular_springboot.discount.DiscountService;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeService;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for executing discount-related chatbot functions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountFunctionExecutor {

    private final DiscountService discountService;
    private final TicketTypeService ticketTypeService;
    private final DiscountTicketTypeService discountTicketTypeService;

    public ChatResponse listDiscounts(String lang) {
        List<Discount> discounts = discountService.findAll();
        if (discounts.isEmpty()) {
            return ChatResponse.builder()
                    .success(true)
                    .message(lang.equals("en") ?
                            "No discounts currently registered." :
                            "No hay descuentos registrados actualmente.")
                    .data(discounts)
                    .build();
        }

        StringBuilder sb = getStringBuilder(lang, discounts);

        return ChatResponse.builder()
                .success(true)
                .message(sb.toString())
                .data(discounts)
                .build();
    }

    private static @NonNull StringBuilder getStringBuilder(String lang, List<Discount> discounts) {
        StringBuilder sb = new StringBuilder(lang.equals("en") ?
                "📋 **Available discounts:**\n\n" :
                "📋 **Descuentos disponibles:**\n\n");

        for (Discount d : discounts) {
            sb.append(String.format("• **ID %d** - %s: `%s` | %d%% | %s: %s\n",
                    d.getId(),
                    lang.equals("en") ? "Code" : "Código",
                    d.getDiscountCode(),
                    d.getDiscountPercentage(),
                    lang.equals("en") ? "Expires" : "Expira",
                    d.getExpiryDate()));
        }
        return sb;
    }

    public ChatResponse getDiscountById(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Discount d = discountService.findById(id);

        // Get associated ticket type names using the service
        List<TicketType> associatedTypes = discountTicketTypeService.findTicketsTypesByDiscountId(id);
        String ticketTypes = associatedTypes.isEmpty() ?
                (lang.equals("en") ? "All" : "Todos") :
                associatedTypes.stream()
                        .map(TicketType::getTypeName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(lang.equals("en") ? "All" : "Todos");

        String message = String.format(lang.equals("en") ?
                        "📋 **Discount Details:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Code:** `%s`\n" +
                        "• **Percentage:** %d%%\n" +
                        "• **Expiry Date:** %s\n" +
                        "• **Applies to ticket types:** %s" :
                        "📋 **Detalles del Descuento:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Código:** `%s`\n" +
                        "• **Porcentaje:** %d%%\n" +
                        "• **Fecha de expiración:** %s\n" +
                        "• **Aplica a tipos de entrada:** %s",
                d.getId(), d.getDiscountCode(), d.getDiscountPercentage(),
                d.getExpiryDate(), ticketTypes);

        return ChatResponse.builder()
                .success(true)
                .message(message)
                .data(d)
                .build();
    }

    public ChatResponse createDiscount(Map<String, Object> args, String lang) {
        String code = (String) args.get("discountCode");
        int percentage = ((Number) args.get("discountPercentage")).intValue();
        String expiryDateStr = (String) args.get("expiryDate");
        @SuppressWarnings("unchecked")
        List<String> ticketTypeNames = (List<String>) args.get("ticketTypeNames");

        List<String> resolvedNames = resolveTicketTypeNames(ticketTypeNames);
        ChatResponse validationError = validateTicketTypeNames(ticketTypeNames, resolvedNames, lang);
        if (validationError != null) return validationError;

        LocalDate expiryDate = LocalDate.parse(expiryDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        ChatResponse dateError = validateExpiryDate(expiryDate, lang);
        if (dateError != null) return dateError;

        Discount discount = Discount.builder()
                .discountCode(code)
                .discountPercentage(percentage)
                .expiryDate(expiryDate)
                .build();

        Discount saved = discountService.save(discount, resolvedNames);
        return buildCreateSuccessResponse(saved, resolvedNames, lang);
    }

    private ChatResponse validateTicketTypeNames(List<String> names, List<String> resolved, String lang) {
        if (resolved.isEmpty() && names != null && !names.isEmpty()) {
            return buildErrorResponse(getMessage(lang,
                    "Could not find any of the specified ticket types. Available types: " + getAvailableTicketTypeNames(),
                    "No se encontró ninguno de los tipos de entrada especificados. Tipos disponibles: " + getAvailableTicketTypeNames()));
        }
        return null;
    }

    private ChatResponse validateExpiryDate(LocalDate expiryDate, String lang) {
        if (expiryDate.isBefore(LocalDate.now())) {
            return buildErrorResponse(getMessage(lang,
                    "The expiry date must be in the future.",
                    "La fecha de expiración debe ser en el futuro."));
        }
        return null;
    }

    private ChatResponse buildCreateSuccessResponse(Discount saved, List<String> resolvedNames, String lang) {
        String ticketTypesStr = resolvedNames.isEmpty() ? getMessage(lang, "All", "Todos") : String.join(", ", resolvedNames);
        return ChatResponse.builder()
                .success(true)
                .message(String.format(getMessage(lang,
                                "✅ Discount created successfully!\n\n• **ID:** %d\n• **Code:** %s\n• **Percentage:** %d%%\n• **Expires:** %s\n• **Applies to:** %s",
                                "✅ ¡Descuento creado exitosamente!\n\n• **ID:** %d\n• **Código:** %s\n• **Porcentaje:** %d%%\n• **Expira:** %s\n• **Aplica a:** %s"),
                        saved.getId(), saved.getDiscountCode(), saved.getDiscountPercentage(),
                        saved.getExpiryDate(), ticketTypesStr))
                .data(saved)
                .build();
    }

    public ChatResponse updateDiscount(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Discount existing = discountService.findById(id);

        String code = getOrDefault(args, "discountCode", existing.getDiscountCode());
        int percentage = getOrDefaultInt(args, "discountPercentage", existing.getDiscountPercentage());
        LocalDate expiryDate = getExpiryDateOrDefault(args, existing.getExpiryDate());

        @SuppressWarnings("unchecked")
        List<String> ticketTypeNames = (List<String>) args.get("ticketTypeNames");
        List<String> resolvedNames = ticketTypeNames != null ? resolveTicketTypeNames(ticketTypeNames) : List.of();

        Discount discount = Discount.builder()
                .discountCode(code)
                .discountPercentage(percentage)
                .expiryDate(expiryDate)
                .build();
        discount.setId(id);

        Discount updated = discountService.update(discount, resolvedNames);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(getMessage(lang,
                                "✅ Discount updated!\n\n• **ID:** %d\n• **Code:** %s\n• **Percentage:** %d%%\n• **Expires:** %s",
                                "✅ ¡Descuento actualizado!\n\n• **ID:** %d\n• **Código:** %s\n• **Porcentaje:** %d%%\n• **Expira:** %s"),
                        updated.getId(), updated.getDiscountCode(),
                        updated.getDiscountPercentage(), updated.getExpiryDate()))
                .data(updated)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(Map<String, Object> args, String key, T defaultValue) {
        return args.containsKey(key) && args.get(key) != null ? (T) args.get(key) : defaultValue;
    }

    private int getOrDefaultInt(Map<String, Object> args, String key, int defaultValue) {
        return args.containsKey(key) && args.get(key) != null ? ((Number) args.get(key)).intValue() : defaultValue;
    }

    private LocalDate getExpiryDateOrDefault(Map<String, Object> args, LocalDate defaultValue) {
        if (args.containsKey("expiryDate") && args.get("expiryDate") != null) {
            return LocalDate.parse((String) args.get("expiryDate"), DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return defaultValue;
    }

    private String getMessage(String lang, String enMessage, String esMessage) {
        return "en".equals(lang) ? enMessage : esMessage;
    }

    public ChatResponse requestDeleteDiscount(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Discount discount = discountService.findById(id);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", id))
                .confirmationMessage(String.format(lang.equals("en") ?
                                "Are you sure you want to delete discount '%s' (ID: %d)?" :
                                "¿Estás seguro de que quieres eliminar el descuento '%s' (ID: %d)?",
                        discount.getDiscountCode(), id))
                .build();

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "⚠️ **Confirmation required**\n\nYou are about to delete the discount:\n• **Code:** %s\n• **Percentage:** %d%%\n• **Expires:** %s\n\nDo you confirm this action?" :
                                "⚠️ **Confirmación requerida**\n\nVas a eliminar el descuento:\n• **Código:** %s\n• **Porcentaje:** %d%%\n• **Expira:** %s\n\n¿Confirmas esta acción?",
                        discount.getDiscountCode(), discount.getDiscountPercentage(), discount.getExpiryDate()))
                .pendingAction(pending)
                .build();
    }

    public ChatResponse executeDeleteDiscount(Long id, String lang) {
        discountService.deleteById(id);
        return ChatResponse.builder()
                .success(true)
                .message(lang.equals("en") ?
                        "✅ Discount deleted successfully." :
                        "✅ Descuento eliminado correctamente.")
                .build();
    }

    // ===== HELPER METHODS =====

    /**
     * Resolve ticket type names with case-insensitive matching
     */
    private List<String> resolveTicketTypeNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return List.of();
        }

        List<TicketType> allTypes = ticketTypeService.findAll();
        return names.stream()
                .map(name -> allTypes.stream()
                        .filter(t -> t.getTypeName().equalsIgnoreCase(name.trim()))
                        .map(TicketType::getTypeName)
                        .findFirst()
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * Get comma-separated list of available ticket type names
     */
    private String getAvailableTicketTypeNames() {
        List<TicketType> types = ticketTypeService.findAll();
        if (types.isEmpty()) {
            return "(none)";
        }
        return types.stream()
                .map(TicketType::getTypeName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("(none)");
    }

    private ChatResponse buildErrorResponse(String message) {
        return ChatResponse.builder()
                .success(false)
                .message("❌ " + message)
                .build();
    }
}

