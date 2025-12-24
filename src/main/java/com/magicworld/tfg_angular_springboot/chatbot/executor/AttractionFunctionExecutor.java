package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionService;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for executing attraction-related chatbot functions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttractionFunctionExecutor {

    private final AttractionService attractionService;

    private static final String DEFAULT_PHOTO_URL = "https://placeholder.com/default.jpg";

    public ChatResponse listAttractions(String lang) {
        List<Attraction> attractions = attractionService.getAllAttractions();
        if (attractions.isEmpty()) {
            return ChatResponse.builder()
                    .success(true)
                    .message(lang.equals("en") ?
                            "No attractions currently registered." :
                            "No hay atracciones registradas actualmente.")
                    .data(attractions)
                    .build();
        }

        StringBuilder sb = getStringBuilder(lang, attractions);

        return ChatResponse.builder()
                .success(true)
                .message(sb.toString())
                .data(attractions)
                .build();
    }

    private static @NonNull StringBuilder getStringBuilder(String lang, List<Attraction> attractions) {
        StringBuilder sb = new StringBuilder(lang.equals("en") ?
                "🎢 **Available attractions:**\n\n" :
                "🎢 **Atracciones disponibles:**\n\n");

        for (Attraction a : attractions) {
            String status = a.getIsActive() ?
                    (lang.equals("en") ? "✅ Active" : "✅ Activa") :
                    (lang.equals("en") ? "❌ Inactive" : "❌ Inactiva");
            sb.append(String.format("• **ID %d** - %s | %s: %s | %s\n",
                    a.getId(), a.getName(),
                    lang.equals("en") ? "Intensity" : "Intensidad",
                    a.getIntensity(), status));
        }
        return sb;
    }

    public ChatResponse getAttractionById(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Attraction a = attractionService.getAttractionById(id);

        String status = a.getIsActive() ?
                (lang.equals("en") ? "Active" : "Activa") :
                (lang.equals("en") ? "Inactive" : "Inactiva");

        String message = String.format(lang.equals("en") ?
                        "🎢 **Attraction Details:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Name:** %s\n" +
                        "• **Intensity:** %s\n" +
                        "• **Description:** %s\n" +
                        "• **Minimum height:** %d cm\n" +
                        "• **Minimum age:** %d years\n" +
                        "• **Minimum weight:** %d kg\n" +
                        "• **Status:** %s\n" +
                        "• **Photo URL:** %s" :
                        "🎢 **Detalles de la Atracción:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Nombre:** %s\n" +
                        "• **Intensidad:** %s\n" +
                        "• **Descripción:** %s\n" +
                        "• **Altura mínima:** %d cm\n" +
                        "• **Edad mínima:** %d años\n" +
                        "• **Peso mínimo:** %d kg\n" +
                        "• **Estado:** %s\n" +
                        "• **URL de foto:** %s",
                a.getId(), a.getName(), a.getIntensity(), a.getDescription(),
                a.getMinimumHeight(), a.getMinimumAge(), a.getMinimumWeight(),
                status, a.getPhotoUrl() != null ? a.getPhotoUrl() : (lang.equals("en") ? "None" : "Ninguna"));

        return ChatResponse.builder()
                .success(true)
                .message(message)
                .data(a)
                .build();
    }

    public ChatResponse createAttraction(Map<String, Object> args, String lang) {
        String name = (String) args.get("name");
        Intensity intensity = Intensity.valueOf(((String) args.get("intensity")).toUpperCase());
        int minHeight = ((Number) args.get("minimumHeight")).intValue();
        int minAge = ((Number) args.get("minimumAge")).intValue();
        int minWeight = ((Number) args.get("minimumWeight")).intValue();
        String description = (String) args.get("description");

        // Get photoUrl - handle both direct URLs and when not provided
        String photoUrl = extractPhotoUrl(args);

        boolean isActive = args.containsKey("isActive") && args.get("isActive") != null ?
                (Boolean) args.get("isActive") : true;

        Attraction attraction = Attraction.builder()
                .name(name)
                .intensity(intensity)
                .minimumHeight(minHeight)
                .minimumAge(minAge)
                .minimumWeight(minWeight)
                .description(description)
                .photoUrl(photoUrl)
                .isActive(isActive)
                .build();

        Attraction saved = attractionService.saveAttraction(attraction);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Attraction created!\n\n• **ID:** %d\n• **Name:** %s\n• **Intensity:** %s\n• **Status:** %s\n• **Photo:** %s" :
                                "✅ ¡Atracción creada!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Intensidad:** %s\n• **Estado:** %s\n• **Foto:** %s",
                        saved.getId(), saved.getName(), saved.getIntensity(),
                        saved.getIsActive() ? (lang.equals("en") ? "Active" : "Activa") :
                                (lang.equals("en") ? "Inactive" : "Inactiva"),
                        saved.getPhotoUrl()))
                .data(saved)
                .build();
    }

    public ChatResponse updateAttraction(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();

        // Fetch existing attraction to preserve fields not being updated
        Attraction existing = attractionService.getAttractionById(id);

        // Only update fields that are provided, otherwise keep existing values
        String name = args.containsKey("name") && args.get("name") != null ?
                (String) args.get("name") : existing.getName();

        Intensity intensity;
        if (args.containsKey("intensity") && args.get("intensity") != null) {
            intensity = Intensity.valueOf(((String) args.get("intensity")).toUpperCase());
        } else {
            intensity = existing.getIntensity();
        }

        int minHeight = args.containsKey("minimumHeight") && args.get("minimumHeight") != null ?
                ((Number) args.get("minimumHeight")).intValue() : existing.getMinimumHeight();
        int minAge = args.containsKey("minimumAge") && args.get("minimumAge") != null ?
                ((Number) args.get("minimumAge")).intValue() : existing.getMinimumAge();
        int minWeight = args.containsKey("minimumWeight") && args.get("minimumWeight") != null ?
                ((Number) args.get("minimumWeight")).intValue() : existing.getMinimumWeight();
        String description = args.containsKey("description") && args.get("description") != null ?
                (String) args.get("description") : existing.getDescription();
        boolean isActive = args.containsKey("isActive") && args.get("isActive") != null ?
                (Boolean) args.get("isActive") : existing.getIsActive();

        // Handle photo URL - only update if explicitly provided
        String photoUrl = null;
        if (args.containsKey("photoUrl") && args.get("photoUrl") != null) {
            String providedUrl = (String) args.get("photoUrl");
            if (!providedUrl.isBlank() && !providedUrl.equals(DEFAULT_PHOTO_URL)) {
                photoUrl = providedUrl;
            }
        }

        Attraction attraction = Attraction.builder()
                .name(name)
                .intensity(intensity)
                .minimumHeight(minHeight)
                .minimumAge(minAge)
                .minimumWeight(minWeight)
                .description(description)
                .photoUrl(photoUrl)
                .isActive(isActive)
                .build();

        Attraction updated = attractionService.updateAttraction(id, attraction);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Attraction updated!\n\n• **ID:** %d\n• **Name:** %s\n• **Intensity:** %s" :
                                "✅ ¡Atracción actualizada!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Intensidad:** %s",
                        updated.getId(), updated.getName(), updated.getIntensity()))
                .data(updated)
                .build();
    }

    public ChatResponse requestDeleteAttraction(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Attraction attraction = attractionService.getAttractionById(id);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteAttraction")
                .params(Map.of("id", id))
                .confirmationMessage(String.format(lang.equals("en") ?
                                "Are you sure you want to delete attraction '%s' (ID: %d)?" :
                                "¿Estás seguro de que quieres eliminar la atracción '%s' (ID: %d)?",
                        attraction.getName(), id))
                .build();

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "⚠️ **Confirmation required**\n\nYou are about to delete the attraction:\n• **Name:** %s\n• **Intensity:** %s\n\nDo you confirm this action?" :
                                "⚠️ **Confirmación requerida**\n\nVas a eliminar la atracción:\n• **Nombre:** %s\n• **Intensidad:** %s\n\n¿Confirmas esta acción?",
                        attraction.getName(), attraction.getIntensity()))
                .pendingAction(pending)
                .build();
    }

    public ChatResponse executeDeleteAttraction(Long id, String lang) {
        attractionService.deleteAttraction(id);
        return ChatResponse.builder()
                .success(true)
                .message(lang.equals("en") ?
                        "✅ Attraction deleted successfully." :
                        "✅ Atracción eliminada correctamente.")
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
}

