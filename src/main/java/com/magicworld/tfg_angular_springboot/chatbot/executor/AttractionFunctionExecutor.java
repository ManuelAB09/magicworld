package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionCategory;
import com.magicworld.tfg_angular_springboot.attraction.AttractionService;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttractionFunctionExecutor {

    private final AttractionService attractionService;

    private static final String DEFAULT_PHOTO_URL = "https://placeholder.com/default.jpg";
    private static final Double DEFAULT_MAP_POSITION_X = 50.0;
    private static final Double DEFAULT_MAP_POSITION_Y = 50.0;

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
            sb.append(String.format("• **ID %d** - %s | %s: %s | %s: %s | 🕘 %s - %s | %s\n",
                    a.getId(), a.getName(),
                    lang.equals("en") ? "Category" : "Categoría",
                    a.getCategory(),
                    lang.equals("en") ? "Intensity" : "Intensidad",
                    a.getIntensity(), 
                    a.getOpeningTime(), a.getClosingTime(), status));
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
                        "• **Category:** %s\n" +
                        "• **Intensity:** %s\n" +
                        "• **Description:** %s\n" +
                        "• **Minimum height:** %d cm\n" +
                        "• **Minimum age:** %d years\n" +
                        "• **Minimum weight:** %d kg\n" +
                        "• **Opening time:** %s\n" +
                        "• **Closing time:** %s\n" +
                        "• **Status:** %s\n" +
                        "• **Map Position:** (%.1f, %.1f)\n" +
                        "• **Photo URL:** %s" :
                        "🎢 **Detalles de la Atracción:**\n\n" +
                        "• **ID:** %d\n" +
                        "• **Nombre:** %s\n" +
                        "• **Categoría:** %s\n" +
                        "• **Intensidad:** %s\n" +
                        "• **Descripción:** %s\n" +
                        "• **Altura mínima:** %d cm\n" +
                        "• **Edad mínima:** %d años\n" +
                        "• **Peso mínimo:** %d kg\n" +
                        "• **Hora de apertura:** %s\n" +
                        "• **Hora de cierre:** %s\n" +
                        "• **Estado:** %s\n" +
                        "• **Posición en mapa:** (%.1f, %.1f)\n" +
                        "• **URL de foto:** %s",
                a.getId(), a.getName(), a.getCategory(), a.getIntensity(), a.getDescription(),
                a.getMinimumHeight(), a.getMinimumAge(), a.getMinimumWeight(),
                a.getOpeningTime(), a.getClosingTime(),
                status, a.getMapPositionX(), a.getMapPositionY(),
                a.getPhotoUrl() != null ? a.getPhotoUrl() : (lang.equals("en") ? "None" : "Ninguna"));

        return ChatResponse.builder()
                .success(true)
                .message(message)
                .data(a)
                .build();
    }

    public ChatResponse createAttraction(Map<String, Object> args, String lang) {
        String name = (String) args.get("name");
        Intensity intensity = Intensity.valueOf(((String) args.get("intensity")).toUpperCase());
        AttractionCategory category = AttractionCategory.valueOf(((String) args.get("category")).toUpperCase());
        int minHeight = ((Number) args.get("minimumHeight")).intValue();
        int minAge = ((Number) args.get("minimumAge")).intValue();
        int minWeight = ((Number) args.get("minimumWeight")).intValue();
        String description = (String) args.get("description");

        String photoUrl = extractPhotoUrl(args);

        boolean isActive = args.containsKey("isActive") && args.get("isActive") != null ?
                (Boolean) args.get("isActive") : true;

        Double mapPositionX = getOrDefaultDouble(args, "mapPositionX", DEFAULT_MAP_POSITION_X);
        Double mapPositionY = getOrDefaultDouble(args, "mapPositionY", DEFAULT_MAP_POSITION_Y);
        LocalTime openingTime = getOrDefaultLocalTime(args, "openingTime", LocalTime.of(9, 0));
        LocalTime closingTime = getOrDefaultLocalTime(args, "closingTime", LocalTime.of(17, 0));

        Attraction attraction = Attraction.builder()
                .name(name)
                .intensity(intensity)
                .category(category)
                .minimumHeight(minHeight)
                .minimumAge(minAge)
                .minimumWeight(minWeight)
                .description(description)
                .photoUrl(photoUrl)
                .isActive(isActive)
                .mapPositionX(mapPositionX)
                .mapPositionY(mapPositionY)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .build();

        Attraction saved = attractionService.saveAttraction(attraction);

        String positionNote = (args.containsKey("mapPositionX") || args.containsKey("mapPositionY")) ? "" :
                (lang.equals("en") ?
                        "\n\n💡 **Tip:** The attraction was placed at the default position (center of map). Use the web form with 3D preview to adjust its exact location." :
                        "\n\n💡 **Consejo:** La atracción se colocó en la posición por defecto (centro del mapa). Usa el formulario web con preview 3D para ajustar su ubicación exacta.");

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Attraction created!\n\n• **ID:** %d\n• **Name:** %s\n• **Category:** %s\n• **Intensity:** %s\n• **Times:** %s - %s\n• **Status:** %s\n• **Map Position:** (%.1f, %.1f)%s" :
                                "✅ ¡Atracción creada!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Categoría:** %s\n• **Intensidad:** %s\n• **Horario:** %s - %s\n• **Estado:** %s\n• **Posición en mapa:** (%.1f, %.1f)%s",
                        saved.getId(), saved.getName(), saved.getCategory(), saved.getIntensity(),
                        saved.getOpeningTime(), saved.getClosingTime(),
                        saved.getIsActive() ? (lang.equals("en") ? "Active" : "Activa") :
                                (lang.equals("en") ? "Inactive" : "Inactiva"),
                        saved.getMapPositionX(), saved.getMapPositionY(), positionNote))
                .data(saved)
                .build();
    }

    public ChatResponse updateAttraction(Map<String, Object> args, String lang) {
        Long id = ((Number) args.get("id")).longValue();
        Attraction existing = attractionService.getAttractionById(id);

        String name = getOrDefault(args, "name", existing.getName());
        Intensity intensity = getIntensityOrDefault(args, existing.getIntensity());
        AttractionCategory category = getCategoryOrDefault(args, existing.getCategory());
        int minHeight = getOrDefaultInt(args, "minimumHeight", existing.getMinimumHeight());
        int minAge = getOrDefaultInt(args, "minimumAge", existing.getMinimumAge());
        int minWeight = getOrDefaultInt(args, "minimumWeight", existing.getMinimumWeight());
        String description = getOrDefault(args, "description", existing.getDescription());
        boolean isActive = getOrDefaultBool(args, "isActive", existing.getIsActive());
        String photoUrl = extractPhotoUrlForUpdate(args);
        Double mapPositionX = getOrDefaultDouble(args, "mapPositionX", existing.getMapPositionX());
        Double mapPositionY = getOrDefaultDouble(args, "mapPositionY", existing.getMapPositionY());
        LocalTime openingTime = getOrDefaultLocalTime(args, "openingTime", existing.getOpeningTime());
        LocalTime closingTime = getOrDefaultLocalTime(args, "closingTime", existing.getClosingTime());

        Attraction attraction = Attraction.builder()
                .name(name)
                .intensity(intensity)
                .category(category)
                .minimumHeight(minHeight)
                .minimumAge(minAge)
                .minimumWeight(minWeight)
                .description(description)
                .photoUrl(photoUrl)
                .isActive(isActive)
                .mapPositionX(mapPositionX)
                .mapPositionY(mapPositionY)
                .openingTime(openingTime)
                .closingTime(closingTime)
                .build();

        Attraction updated = attractionService.updateAttraction(id, attraction);

        return ChatResponse.builder()
                .success(true)
                .message(String.format(lang.equals("en") ?
                                "✅ Attraction updated!\n\n• **ID:** %d\n• **Name:** %s\n• **Category:** %s\n• **Intensity:** %s\n• **Times:** %s - %s\n• **Map Position:** (%.1f, %.1f)" :
                                "✅ ¡Atracción actualizada!\n\n• **ID:** %d\n• **Nombre:** %s\n• **Categoría:** %s\n• **Intensidad:** %s\n• **Horario:** %s - %s\n• **Posición en mapa:** (%.1f, %.1f)",
                        updated.getId(), updated.getName(), updated.getCategory(), updated.getIntensity(),
                        updated.getOpeningTime(), updated.getClosingTime(),
                        updated.getMapPositionX(), updated.getMapPositionY()))
                .data(updated)
                .build();
    }

    private String extractPhotoUrlForUpdate(Map<String, Object> args) {
        return TicketTypeFunctionExecutor.getString(args, DEFAULT_PHOTO_URL);
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(Map<String, Object> args, String key, T defaultValue) {
        return args.containsKey(key) && args.get(key) != null ? (T) args.get(key) : defaultValue;
    }

    private int getOrDefaultInt(Map<String, Object> args, String key, int defaultValue) {
        return args.containsKey(key) && args.get(key) != null ? ((Number) args.get(key)).intValue() : defaultValue;
    }

    private boolean getOrDefaultBool(Map<String, Object> args, String key, boolean defaultValue) {
        return args.containsKey(key) && args.get(key) != null ? (Boolean) args.get(key) : defaultValue;
    }

    private Intensity getIntensityOrDefault(Map<String, Object> args, Intensity defaultValue) {
        if (args.containsKey("intensity") && args.get("intensity") != null) {
            return Intensity.valueOf(((String) args.get("intensity")).toUpperCase());
        }
        return defaultValue;
    }

    private AttractionCategory getCategoryOrDefault(Map<String, Object> args, AttractionCategory defaultValue) {
        if (args.containsKey("category") && args.get("category") != null) {
            return AttractionCategory.valueOf(((String) args.get("category")).toUpperCase());
        }
        return defaultValue;
    }

    private Double getOrDefaultDouble(Map<String, Object> args, String key, Double defaultValue) {
        if (args.containsKey(key) && args.get(key) != null) {
            return ((Number) args.get(key)).doubleValue();
        }
        return defaultValue;
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

    private LocalTime getOrDefaultLocalTime(Map<String, Object> args, String key, LocalTime defaultValue) {
        if (args.containsKey(key) && args.get(key) != null) {
            try {
                return LocalTime.parse((String) args.get(key));
            } catch (Exception e) {
                log.warn("Invalid time format for {}: {}", key, args.get(key));
            }
        }
        return defaultValue;
    }

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

