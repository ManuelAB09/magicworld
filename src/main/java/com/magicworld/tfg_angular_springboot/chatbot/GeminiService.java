package com.magicworld.tfg_angular_springboot.chatbot;

import com.google.genai.Client;
import com.google.genai.types.*;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String modelName;

    private final ChatbotToolsBuilder toolsBuilder;
    private final ChatbotFunctionExecutor functionExecutor;

    private Client client;

    @PostConstruct
    public void init() {
        this.client = Client.builder().apiKey(apiKey).build();
    }

    public ChatResponse processMessage(ChatRequest request) {
        try {
            String detectedLanguage = detectLanguage(request.getMessage());
            log.debug("Detected language: {}", detectedLanguage);


            if (request.getPendingAction() != null) {
                return functionExecutor.executePendingAction(request, detectedLanguage);
            }


            List<Content> contents = buildContents(request);


            List<Tool> tools = toolsBuilder.buildTools();


            GenerateContentConfig config = GenerateContentConfig.builder()
                    .tools(tools)
                    .systemInstruction(Content.builder()
                            .parts(List.of(Part.builder()
                                    .text(getSystemPrompt(detectedLanguage))
                                    .build()))
                            .build())
                    .build();


            GenerateContentResponse response = client.models.generateContent(
                    modelName,
                    contents,
                    config
            );


            return processGeminiResponse(response, detectedLanguage);

        } catch (Exception e) {
            log.error("Error processing message with Gemini", e);
            return ChatResponse.builder()
                    .success(false)
                    .message("❌ Error processing your request: " + e.getMessage())
                    .build();
        }
    }

    private static final Set<String> ENGLISH_PATTERNS = Set.of(
            "create", "delete", "update", "list", "show", "get", "add", "remove",
            "please", "want", "would", "could", "can you", "i need", "help me",
            "the", "this", "that", "what", "when", "where", "how", "why",
            "discount", "attraction", "ticket", "type", "active", "inactive",
            "yes", "no", "confirm", "cancel", "name", "price", "description"
    );

    private static final Set<String> SPANISH_PATTERNS = Set.of(
            "crear", "borrar", "eliminar", "actualizar", "listar", "mostrar", "añadir",
            "por favor", "quiero", "necesito", "puedes", "ayuda", "ayúdame",
            "el", "la", "los", "las", "que", "qué", "cuando", "cuándo", "donde", "dónde",
            "cómo", "por qué", "descuento", "atracción", "entrada", "tipo", "activo",
            "sí", "no", "confirmar", "cancelar", "nombre", "precio", "descripción"
    );

    private String detectLanguage(String message) {
        if (message == null || message.isBlank()) {
            return "es";
        }

        String lowerMessage = message.toLowerCase();

        long englishScore = ENGLISH_PATTERNS.stream().filter(lowerMessage::contains).count();
        long spanishScore = SPANISH_PATTERNS.stream().filter(lowerMessage::contains).count();

        if (lowerMessage.matches(".*[áéíóúüñ¿¡].*")) {
            spanishScore += 3;
        }

        log.debug("Language detection - EN: {}, ES: {}", englishScore, spanishScore);

        return englishScore > spanishScore ? "en" : "es";
    }

    private String getSystemPrompt(String language) {
        if ("en".equals(language)) {
            return """
                    You are an administration assistant for MagicWorld, an amusement park.
                    Your role is to help administrators manage:
                    
                    1. **Discounts**: Have code, percentage (1-100) and expiry date
                    2. **Ticket Types**: Have name, cost, currency, description, max per day and photo URL
                    3. **Attractions**: Have name, intensity (LOW/MEDIUM/HIGH), minimum height, minimum age, minimum weight, description, photo URL and active status
                    
                    IMPORTANT RULES:
                    - ALWAYS respond in English since the user is writing in English
                    - For DELETE operations, ALWAYS request confirmation first
                    - When creating or updating, make sure you have all necessary data
                    - Be friendly and clear in your responses
                    - Use list functions to show information when requested
                    - If no photo URL is provided, use "https://placeholder.com/default.jpg"
                    - When the user mentions a ticket type by name (like "Child", "Adult", "Senior"), use case-insensitive matching
                    - ALWAYS execute the requested function. Do not just describe what you would do - actually call the function
                    - If you need to find a ticket type by name, use the findTicketTypeByName function first
                    
                    Remember: Ticket type names are unique and used to associate discounts.
                    
                    CRITICAL: When the user asks you to do something (create, list, delete, update), you MUST call the appropriate function. Do not just respond with text.
                    """;
        }

        return """
                Eres un asistente de administración para MagicWorld, un parque de atracciones.
                Tu rol es ayudar a los administradores a gestionar:
                
                1. **Descuentos**: Tienen código, porcentaje (1-100) y fecha de expiración
                2. **Tipos de entrada**: Tienen nombre, costo, moneda, descripción, máximo por día y URL de foto
                3. **Atracciones**: Tienen nombre, intensidad (LOW/MEDIUM/HIGH), altura mínima, edad mínima, peso mínimo, descripción, URL de foto y estado activo
                
                REGLAS IMPORTANTES:
                - SIEMPRE responde en español ya que el usuario está escribiendo en español
                - Para operaciones de BORRADO, SIEMPRE pide confirmación primero
                - Cuando crees o actualices, asegúrate de tener todos los datos necesarios
                - Sé amable y claro en tus respuestas
                - Usa las funciones de listar para mostrar información cuando se solicite
                - Si no se proporciona URL de foto, usa "https://placeholder.com/default.jpg"
                - Cuando el usuario mencione un tipo de entrada por nombre (como "Child", "Niño", "Adulto"), haz coincidencia sin distinguir mayúsculas/minúsculas
                - SIEMPRE ejecuta la función solicitada. No solo describas lo que harías - realmente llama a la función
                - Si necesitas encontrar un tipo de entrada por nombre, usa primero la función findTicketTypeByName
                
                Recuerda: Los tipos de entrada tienen nombres únicos que se usan para asociar descuentos.
                
                CRÍTICO: Cuando el usuario te pida hacer algo (crear, listar, eliminar, actualizar), DEBES llamar a la función apropiada. No solo respondas con texto.
                """;
    }

    /**
     * Build content list from chat history
     */
    private List<Content> buildContents(ChatRequest request) {
        List<Content> contents = new ArrayList<>();

        // Add history if exists
        if (request.getHistory() != null) {
            for (ChatRequest.ChatHistoryEntry entry : request.getHistory()) {
                contents.add(Content.builder()
                        .role(entry.getRole().equals("user") ? "user" : "model")
                        .parts(List.of(Part.builder().text(entry.getContent()).build()))
                        .build());
            }
        }

        // Add current message
        contents.add(Content.builder()
                .role("user")
                .parts(List.of(Part.builder().text(request.getMessage()).build()))
                .build());

        return contents;
    }

    private ChatResponse processGeminiResponse(GenerateContentResponse response, String detectedLanguage) {
        Optional<Content> contentOpt = extractContent(response);
        if (contentOpt.isEmpty()) {
            return buildEmptyResponse(detectedLanguage);
        }

        Content content = contentOpt.get();
        if (content.parts().isEmpty()) {
            return buildNoContentResponse(detectedLanguage);
        }

        return processContentParts(content.parts().get(), detectedLanguage);
    }

    private Optional<Content> extractContent(GenerateContentResponse response) {
        if (response.candidates().isEmpty()) {
            return Optional.empty();
        }
        Candidate candidate = response.candidates().get().getFirst();
        return candidate.content();
    }

    private ChatResponse buildEmptyResponse(String lang) {
        return ChatResponse.builder()
                .success(false)
                .message("en".equals(lang) ? "No response received from the AI." : "No se recibió respuesta del AI.")
                .build();
    }

    private ChatResponse buildNoContentResponse(String lang) {
        return ChatResponse.builder()
                .success(false)
                .message("en".equals(lang) ? "No content in the AI response." : "Sin contenido en la respuesta del AI.")
                .build();
    }

    private ChatResponse processContentParts(List<Part> parts, String detectedLanguage) {
        for (Part part : parts) {
            if (part.functionCall().isPresent()) {
                FunctionCall functionCall = part.functionCall().get();
                String functionName = functionCall.name().orElse("");
                Map<String, Object> args = functionCall.args().orElse(new HashMap<>());
                return functionExecutor.executeFunction(functionName, args, detectedLanguage);
            }
            if (part.text().isPresent()) {
                return ChatResponse.builder()
                        .success(true)
                        .message(part.text().get())
                        .build();
            }
        }

        return ChatResponse.builder()
                .success(true)
                .message("en".equals(detectedLanguage) ? "Understood, how else can I help you?" : "Entendido, ¿en qué más puedo ayudarte?")
                .build();
    }
}

