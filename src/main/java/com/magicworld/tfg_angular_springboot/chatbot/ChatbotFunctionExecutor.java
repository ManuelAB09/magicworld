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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotFunctionExecutor {

    private final DiscountFunctionExecutor discountExecutor;
    private final TicketTypeFunctionExecutor ticketTypeExecutor;
    private final AttractionFunctionExecutor attractionExecutor;

    public ChatResponse executeFunction(String functionName, Map<String, Object> args, String detectedLanguage) {
        log.info("Executing function: {} with args: {}", functionName, args);

        try {
            return switch (functionName) {
                // Discounts
                case "listDiscounts" -> discountExecutor.listDiscounts(detectedLanguage);
                case "getDiscountById" -> discountExecutor.getDiscountById(args, detectedLanguage);
                case "createDiscount" -> discountExecutor.createDiscount(args, detectedLanguage);
                case "updateDiscount" -> discountExecutor.updateDiscount(args, detectedLanguage);
                case "requestDeleteDiscount" -> discountExecutor.requestDeleteDiscount(args, detectedLanguage);

                // Ticket Types
                case "listTicketTypes" -> ticketTypeExecutor.listTicketTypes(detectedLanguage);
                case "getTicketTypeById" -> ticketTypeExecutor.getTicketTypeById(args, detectedLanguage);
                case "findTicketTypeByName" -> ticketTypeExecutor.findTicketTypeByName(args, detectedLanguage);
                case "createTicketType" -> ticketTypeExecutor.createTicketType(args, detectedLanguage);
                case "updateTicketType" -> ticketTypeExecutor.updateTicketType(args, detectedLanguage);
                case "requestDeleteTicketType" -> ticketTypeExecutor.requestDeleteTicketType(args, detectedLanguage);

                // Attractions
                case "listAttractions" -> attractionExecutor.listAttractions(detectedLanguage);
                case "getAttractionById" -> attractionExecutor.getAttractionById(args, detectedLanguage);
                case "createAttraction" -> attractionExecutor.createAttraction(args, detectedLanguage);
                case "updateAttraction" -> attractionExecutor.updateAttraction(args, detectedLanguage);
                case "requestDeleteAttraction" -> attractionExecutor.requestDeleteAttraction(args, detectedLanguage);

                default -> buildErrorResponse(
                        detectedLanguage.equals("en") ?
                                "Unknown function: " + functionName :
                                "Función no reconocida: " + functionName
                );
            };
        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return buildErrorResponse(
                    detectedLanguage.equals("en") ?
                            "Invalid date format. Please use YYYY-MM-DD format (e.g., 2025-12-31)." :
                            "Formato de fecha inválido. Por favor, usa el formato AAAA-MM-DD (ej: 2025-12-31)."
            );
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        } catch (Exception e) {
            log.error("Error executing function {}: {}", functionName, e.getMessage(), e);
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        }
    }

    /**
     * Execute a pending confirmation action
     */
    public ChatResponse executePendingAction(ChatRequest request, String detectedLanguage) {
        PendingAction pending = request.getPendingAction();
        String message = request.getMessage().toLowerCase();

        // Check confirmation keywords in both languages
        boolean confirmed = message.contains("sí") || message.contains("si") ||
                message.contains("yes") || message.contains("confirmo") ||
                message.contains("confirm") || message.contains("ok") ||
                message.contains("adelante") || message.contains("proceed");

        if (!confirmed) {
            return ChatResponse.builder()
                    .success(true)
                    .message(detectedLanguage.equals("en") ?
                            "❌ Operation cancelled. How else can I help you?" :
                            "❌ Operación cancelada. ¿En qué más puedo ayudarte?")
                    .build();
        }

        Long id = ((Number) pending.getParams().get("id")).longValue();

        try {
            return switch (pending.getActionType()) {
                case "deleteDiscount" -> discountExecutor.executeDeleteDiscount(id, detectedLanguage);
                case "deleteTicketType" -> ticketTypeExecutor.executeDeleteTicketType(id, detectedLanguage);
                case "deleteAttraction" -> attractionExecutor.executeDeleteAttraction(id, detectedLanguage);
                default -> buildErrorResponse(
                        detectedLanguage.equals("en") ? "Unknown action" : "Acción no reconocida"
                );
            };
        } catch (Exception e) {
            log.error("Error executing pending action: {}", e.getMessage());
            return buildErrorResponse(interpretError(e.getMessage(), detectedLanguage));
        }
    }

    /**
     * Interpret technical errors into user-friendly messages
     */
    private String interpretError(String errorMessage, String lang) {
        if (errorMessage == null) {
            return lang.equals("en") ?
                    "An unexpected error occurred. Please try again." :
                    "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo.";
        }

        String lowerError = errorMessage.toLowerCase();

        // Duplicate entry errors
        if (lowerError.contains("duplicate") || lowerError.contains("already exists") ||
                lowerError.contains("unique") || lowerError.contains("constraint")) {
            return lang.equals("en") ?
                    "This item already exists. Please use a different name or code." :
                    "Este elemento ya existe. Por favor, usa un nombre o código diferente.";
        }

        // Not found errors
        if (lowerError.contains("not found") || lowerError.contains("no encontr")) {
            return lang.equals("en") ?
                    "The requested item was not found. Please verify the ID or name." :
                    "El elemento solicitado no fue encontrado. Por favor, verifica el ID o nombre.";
        }

        // Validation errors
        if (lowerError.contains("validation") || lowerError.contains("invalid") ||
                lowerError.contains("must be") || lowerError.contains("cannot be")) {
            return lang.equals("en") ?
                    "The provided data is invalid. Please check your input and try again." :
                    "Los datos proporcionados no son válidos. Por favor, revisa tu entrada e inténtalo de nuevo.";
        }

        // Date errors
        if (lowerError.contains("date") || lowerError.contains("fecha")) {
            return lang.equals("en") ?
                    "There was a problem with the date. Make sure it's in the correct format (YYYY-MM-DD) and is a valid date." :
                    "Hubo un problema con la fecha. Asegúrate de que esté en el formato correcto (AAAA-MM-DD) y sea una fecha válida.";
        }

        // Percentage errors
        if (lowerError.contains("percentage") || lowerError.contains("porcentaje")) {
            return lang.equals("en") ?
                    "The percentage must be between 1 and 100." :
                    "El porcentaje debe estar entre 1 y 100.";
        }

        // Foreign key / relationship errors
        if (lowerError.contains("foreign key") || lowerError.contains("reference") ||
                lowerError.contains("in use") || lowerError.contains("associated")) {
            return lang.equals("en") ?
                    "This item cannot be deleted because it's being used by other records." :
                    "Este elemento no puede ser eliminado porque está siendo utilizado por otros registros.";
        }

        // Default message
        return lang.equals("en") ?
                "An error occurred while processing your request. Please check the data and try again." :
                "Ha ocurrido un error al procesar tu solicitud. Por favor, verifica los datos e inténtalo de nuevo.";
    }

    private ChatResponse buildErrorResponse(String message) {
        return ChatResponse.builder()
                .success(false)
                .message("❌ " + message)
                .build();
    }
}

