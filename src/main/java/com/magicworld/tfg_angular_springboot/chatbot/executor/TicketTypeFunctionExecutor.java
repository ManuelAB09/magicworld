package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for executing ticket type-related chatbot functions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketTypeFunctionExecutor {

    private final TicketTypeService ticketTypeService;

    private static final String DEFAULT_PHOTO_URL = "https://placeholder.com/default.jpg";

    public ChatResponse listTicketTypes(String lang) {
        List<TicketType> ticketTypes = ticketTypeService.findAll();
        if (ticketTypes.isEmpty()) {
            return ChatResponse.builder()
                    .success(true)
                    .message(lang.equals("en") ?
                            "No ticket types currently registered." :
                            "No hay tipos de entrada registrados actualmente.")
                    .data(ticketTypes)
                    .build();
        }

        StringBuilder sb = new StringBuilder(lang.equals("en") ?
                "🎫 **Available ticket types:**\n\n" :
                "🎫 **Tipos de entrada disponibles:**\n\n");

        for (TicketType t : ticketTypes) {
            sb.append(String.format("• **ID %d** - %s | %.2f %s | Max/%s: %d\n",
                    t.getId(), t.getTypeName(), t.getCost(), t.getCurrency(),
                    lang.equals("en") ? "day" : "día", t.getMaxPerDay()));
        }

        return ChatResponse.builder()
                .success(true)
                .message(sb.toString())
                .data(ticketTypes)
                .build();
    }

    public ChatResponse getTicketTypeById(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        TicketType t = ticketTypeService.findById(id);

        String message = String.format(lang.equals("en") ?
                        "🎫 **Ticket Type Details:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Name:** %s\n" +
                        "• **Price:** %.2f %s\n" +
                        "• **Description:** %s\n" +
                        "• **Max per day:** %d\n" +
                        "• **Photo URL:** %s" :
                        "🎫 **Detalles del Tipo de Entrada:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Nombre:** %s\n" +
                        "• **Precio:** %.2f %s\n" +
                        "• **Descripción:** %s\n" +
                        "• **Máximo por día:** %d\n" +
                        "• **URL de foto:** %s",
                t.getId(), t.getTypeName(), t.getCost(), t.getCurrency(),
                t.getDescription(), t.getMaxPerDay(),
                t.getPhotoUrl() != null ? t.getPhotoUrl() : (lang.equals("en") ? "None" : "Ninguna"));

        return ChatResponse.builder()
                .success(true)
                .message(message)
                .data(t)
                .build();
    }

    public ChatResponse findTicketTypeByName(Map<String, Object> args, String lang) {
        String name = (String) args.get("name");
        Optional<TicketType> found = findTicketTypeByNameIgnoreCase(name);

        if (found.isEmpty()) {
            return ChatResponse.builder()
                    .success(true)
                    .message(String.format(lang.equals("en") ?
                                    "No ticket type found with name '%s'. Available types: %s" :
                                    "No se encontró tipo de entrada con nombre '%s'. Tipos disponibles: %s",
                            name, getAvailableTicketTypeNames()))
                    .build();
        }

        TicketType t = found.get();
        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "🎫 **Found ticket type:**\n\n• **ID:** %d\n• **Name:** %s\n• **Price:** %.2f %s\n• **Max/day:** %d\n• **Description:** %s" :
                                "🎫 **Tipo de entrada encontrado:**\n\n• **ID:** %d\n• **Nombre:** %s\n• **Precio:** %.2f %s\n• **Máx/día:** %d\n• **Descripción:** %s",
                        t.getId(), t.getTypeName(), t.getCost(), t.getCurrency(),
                        t.getMaxPerDay(), t.getDescription()))
                .data(t)
                .build();
    }

    public ChatResponse createTicketType(Map<String, Object> args, String lang) {
        String typeName = (String) args.get("typeName");
        BigDecimal cost = BigDecimal.valueOf(((Number) args.get("cost")).doubleValue());
        String currency = (String) args.get("currency");
        String description = (String) args.get("description");
        int maxPerDay = ((Number) args.get("maxPerDay")).intValue();

        // Get photoUrl - handle both direct URLs and when not provided
        String photoUrl = extractPhotoUrl(args);

        TicketType ticketType = TicketType.builder()
                .typeName(typeName)
                .cost(cost)
                .currency(currency)
                .description(description)
                .maxPerDay(maxPerDay)
                .photoUrl(photoUrl)
                .build();

        TicketType saved = ticketTypeService.save(ticketType);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Ticket type created!\n\n• **ID:** %d\n• **Name:** %s\n• **Price:** %.2f %s\n• **Max/day:** %d\n• **Photo:** %s" :
                                "✅ ¡Tipo de entrada creado!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Precio:** %.2f %s\n• **Máx/día:** %d\n• **Foto:** %s",
                        saved.getId(), saved.getTypeName(), saved.getCost(),
                        saved.getCurrency(), saved.getMaxPerDay(), saved.getPhotoUrl()))
                .data(saved)
                .build();
    }

    public ChatResponse updateTicketType(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();

        // Fetch existing ticket type to preserve fields not being updated
        TicketType existing = ticketTypeService.findById(id);

        // Only update fields that are provided, otherwise keep existing values
        String typeName = args.containsKey("typeName") && args.get("typeName") != null ?
                (String) args.get("typeName") : existing.getTypeName();
        BigDecimal cost = args.containsKey("cost") && args.get("cost") != null ?
                BigDecimal.valueOf(((Number) args.get("cost")).doubleValue()) : existing.getCost();
        String currency = args.containsKey("currency") && args.get("currency") != null ?
                (String) args.get("currency") : existing.getCurrency();
        String description = args.containsKey("description") && args.get("description") != null ?
                (String) args.get("description") : existing.getDescription();
        int maxPerDay = args.containsKey("maxPerDay") && args.get("maxPerDay") != null ?
                ((Number) args.get("maxPerDay")).intValue() : existing.getMaxPerDay();

        // Handle photo URL - only update if explicitly provided
        String photoUrl = null;
        if (args.containsKey("photoUrl") && args.get("photoUrl") != null) {
            String providedUrl = (String) args.get("photoUrl");
            if (!providedUrl.isBlank() && !providedUrl.equals(DEFAULT_PHOTO_URL)) {
                photoUrl = providedUrl;
            }
        }

        TicketType ticketType = TicketType.builder()
                .typeName(typeName)
                .cost(cost)
                .currency(currency)
                .description(description)
                .maxPerDay(maxPerDay)
                .photoUrl(photoUrl)
                .build();

        TicketType updated = ticketTypeService.update(id, ticketType);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Ticket type updated!\n\n• **ID:** %d\n• **Name:** %s\n• **Price:** %.2f %s" :
                                "✅ ¡Tipo de entrada actualizado!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Precio:** %.2f %s",
                        updated.getId(), updated.getTypeName(), updated.getCost(), updated.getCurrency()))
                .data(updated)
                .build();
    }

    public ChatResponse requestDeleteTicketType(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        TicketType ticketType = ticketTypeService.findById(id);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteTicketType")
                .params(Map.of("id", id))
                .confirmationMessage(String.format(lang.equals("en") ?
                                "Are you sure you want to delete ticket type '%s' (ID: %d)?" :
                                "¿Estás seguro de que quieres eliminar el tipo de entrada '%s' (ID: %d)?",
                        ticketType.getTypeName(), id))
                .build();

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "⚠️ **Confirmation required**\n\nYou are about to delete the ticket type:\n• **Name:** %s\n• **Price:** %.2f %s\n\nDo you confirm this action?" :
                                "⚠️ **Confirmación requerida**\n\nVas a eliminar el tipo de entrada:\n• **Nombre:** %s\n• **Precio:** %.2f %s\n\n¿Confirmas esta acción?",
                        ticketType.getTypeName(), ticketType.getCost(), ticketType.getCurrency()))
                .pendingAction(pending)
                .build();
    }

    public ChatResponse executeDeleteTicketType(Long id, String lang) {
        ticketTypeService.delete(id);
        return ChatResponse.builder()
                .success(true)
                .message(lang.equals("en") ?
                        "✅ Ticket type deleted successfully." :
                        "✅ Tipo de entrada eliminado correctamente.")
                .build();
    }

    // ===== HELPER METHODS =====

    /**
     * Extract photo URL from args, validating it's a proper URL
     */
    private String extractPhotoUrl(Map<String, Object> args) {
        if (!args.containsKey("photoUrl") || args.get("photoUrl") == null) {
            return DEFAULT_PHOTO_URL;
        }

        String url = (String) args.get("photoUrl");
        if (url.isBlank()) {
            return DEFAULT_PHOTO_URL;
        }

        // Validate that it looks like a URL
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        return DEFAULT_PHOTO_URL;
    }

    /**
     * Find ticket type by name (case insensitive)
     */
    private Optional<TicketType> findTicketTypeByNameIgnoreCase(String name) {
        return ticketTypeService.findAll().stream()
                .filter(t -> t.getTypeName().equalsIgnoreCase(name.trim()))
                .findFirst();
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
}

