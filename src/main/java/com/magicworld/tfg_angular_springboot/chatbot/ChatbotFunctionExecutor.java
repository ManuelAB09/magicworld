package com.magicworld.tfg_angular_springboot.chatbot;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import com.magicworld.tfg_angular_springboot.chatbot.executor.AttractionFunctionExecutor;
import com.magicworld.tfg_angular_springboot.chatbot.executor.DiscountFunctionExecutor;
import com.magicworld.tfg_angular_springboot.chatbot.executor.TicketTypeFunctionExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotFunctionExecutor {

    private final DiscountFunctionExecutor discountExecutor;
    private final TicketTypeFunctionExecutor ticketTypeExecutor;
    private final AttractionFunctionExecutor attractionExecutor;

    private Map<String, BiFunction<Map<String, Object>, String, ChatResponse>> functionRegistry;
    private Map<String, BiFunction<Long, String, ChatResponse>> deleteActionRegistry;

    private static final Set<String> CONFIRMATION_KEYWORDS = Set.of(
            "sí", "si", "yes", "confirmo", "confirm", "ok", "adelante", "proceed"
    );

    private record ErrorPattern(List<String> keywords, String enMessage, String esMessage) {}

    private static final List<ErrorPattern> ERROR_PATTERNS = List.of(
            new ErrorPattern(
                    List.of("duplicate", "already exists", "unique", "constraint"),
                    "This item already exists. Please use a different name or code.",
                    "Este elemento ya existe. Por favor, usa un nombre o código diferente."
            ),
            new ErrorPattern(
                    List.of("not found", "no encontr"),
                    "The requested item was not found. Please verify the ID or name.",
                    "El elemento solicitado no fue encontrado. Por favor, verifica el ID o nombre."
            ),
            new ErrorPattern(
                    List.of("validation", "invalid", "must be", "cannot be"),
                    "The provided data is invalid. Please check your input and try again.",
                    "Los datos proporcionados no son válidos. Por favor, revisa tu entrada e inténtalo de nuevo."
            ),
            new ErrorPattern(
                    List.of("date", "fecha"),
                    "There was a problem with the date. Make sure it's in the correct format (YYYY-MM-DD) and is a valid date.",
                    "Hubo un problema con la fecha. Asegúrate de que esté en el formato correcto (AAAA-MM-DD) y sea una fecha válida."
            ),
            new ErrorPattern(
                    List.of("percentage", "porcentaje"),
                    "The percentage must be between 1 and 100.",
                    "El porcentaje debe estar entre 1 y 100."
            ),
            new ErrorPattern(
                    List.of("foreign key", "reference", "in use", "associated"),
                    "This item cannot be deleted because it's being used by other records.",
                    "Este elemento no puede ser eliminado porque está siendo utilizado por otros registros."
            )
    );

    private void ensureInitialized() {
        if (functionRegistry == null) {
            functionRegistry = Map.ofEntries(
                    Map.entry("listDiscounts", (args, lang) -> discountExecutor.listDiscounts(lang)),
                    Map.entry("getDiscountById", discountExecutor::getDiscountById),
                    Map.entry("createDiscount", discountExecutor::createDiscount),
                    Map.entry("updateDiscount", discountExecutor::updateDiscount),
                    Map.entry("requestDeleteDiscount", discountExecutor::requestDeleteDiscount),
                    Map.entry("listTicketTypes", (args, lang) -> ticketTypeExecutor.listTicketTypes(lang)),
                    Map.entry("getTicketTypeById", ticketTypeExecutor::getTicketTypeById),
                    Map.entry("findTicketTypeByName", ticketTypeExecutor::findTicketTypeByName),
                    Map.entry("createTicketType", ticketTypeExecutor::createTicketType),
                    Map.entry("updateTicketType", ticketTypeExecutor::updateTicketType),
                    Map.entry("requestDeleteTicketType", ticketTypeExecutor::requestDeleteTicketType),
                    Map.entry("listAttractions", (args, lang) -> attractionExecutor.listAttractions(lang)),
                    Map.entry("getAttractionById", attractionExecutor::getAttractionById),
                    Map.entry("createAttraction", attractionExecutor::createAttraction),
                    Map.entry("updateAttraction", attractionExecutor::updateAttraction),
                    Map.entry("requestDeleteAttraction", attractionExecutor::requestDeleteAttraction)
            );
        }
        if (deleteActionRegistry == null) {
            deleteActionRegistry = Map.of(
                    "deleteDiscount", discountExecutor::executeDeleteDiscount,
                    "deleteTicketType", ticketTypeExecutor::executeDeleteTicketType,
                    "deleteAttraction", attractionExecutor::executeDeleteAttraction
            );
        }
    }

    public ChatResponse executeFunction(String functionName, Map<String, Object> args, String detectedLanguage) {
        ensureInitialized();
        log.info("Executing function: {} with args: {}", functionName, args);

        try {
            BiFunction<Map<String, Object>, String, ChatResponse> handler = functionRegistry.get(functionName);
            if (handler == null) {
                return buildErrorResponse(getMessage(detectedLanguage,
                        "Unknown function: " + functionName,
                        "Función no reconocida: " + functionName));
            }
            return handler.apply(args, detectedLanguage);
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return buildErrorResponse(getMessage(detectedLanguage,
                    "Invalid date format. Please use YYYY-MM-DD format (e.g., 2025-12-31).",
                    "Formato de fecha inválido. Por favor, usa el formato AAAA-MM-DD (ej: 2025-12-31)."));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        } catch (Exception e) {
            log.error("Error executing function {}: {}", functionName, e.getMessage(), e);
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        }
    }

    public ChatResponse executePendingAction(ChatRequest request, String detectedLanguage) {
        ensureInitialized();
        PendingAction pending = request.getPendingAction();
        String message = request.getMessage().toLowerCase();

        if (!isConfirmed(message)) {
            return ChatResponse.builder()
                    .success(true)
                    .message(getMessage(detectedLanguage,
                            "❌ Operation cancelled. How else can I help you?",
                            "❌ Operación cancelada. ¿En qué más puedo ayudarte?"))
                    .build();
        }

        Long id = ((Number) pending.getParams().get("id")).longValue();

        try {
            BiFunction<Long, String, ChatResponse> handler = deleteActionRegistry.get(pending.getActionType());
            if (handler == null) {
                return buildErrorResponse(getMessage(detectedLanguage, "Unknown action", "Acción no reconocida"));
            }
            return handler.apply(id, detectedLanguage);
        } catch (Exception e) {
            log.error("Error executing pending action: {}", e.getMessage());
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        }
    }

    private boolean isConfirmed(String message) {
        return CONFIRMATION_KEYWORDS.stream().anyMatch(message::contains);
    }

    private String interpretError(String errorMessage, String lang) {
        if (errorMessage == null) {
            return getMessage(lang,
                    "An unexpected error occurred. Please try again.",
                    "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo.");
        }

        String lowerError = errorMessage.toLowerCase();

        return ERROR_PATTERNS.stream()
                .filter(pattern -> pattern.keywords().stream().anyMatch(lowerError::contains))
                .findFirst()
                .map(pattern -> getMessage(lang, pattern.enMessage(), pattern.esMessage()))
                .orElse(getMessage(lang,
                        "An error occurred while processing your request. Please check the data and try again.",
                        "Ha ocurrido un error al procesar tu solicitud. Por favor, verifica los datos e inténtalo de nuevo."));
    }

    private String getMessage(String lang, String enMessage, String esMessage) {
        return "en".equals(lang) ? enMessage : esMessage;
    }

    private ChatResponse buildErrorResponse(String message) {
        return ChatResponse.builder()
                .success(false)
                .message("❌ " + message)
                .build();
    }
}

