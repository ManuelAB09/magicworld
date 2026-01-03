package com.magicworld.tfg_angular_springboot.chatbot;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Servicio Gemini")
public class GeminiServiceTests {

    @Mock
    private ChatbotToolsBuilder toolsBuilder;

    @Mock
    private ChatbotFunctionExecutor functionExecutor;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(toolsBuilder, functionExecutor);
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiService, "modelName", "gemini-2.0-flash");
    }

    @Test
    @Story("Procesar Mensaje con Acción Pendiente")
    @Description("Verifica que se delega a functionExecutor con acción pendiente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Procesar mensaje con acción pendiente delega a executor")
    void testProcessMessageWithPendingActionDelegatesToFunctionExecutor() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Confirmed")
                .build();
        when(functionExecutor.executePendingAction(any(), any())).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Procesar Mensaje con Acción Pendiente")
    @Description("Verifica que retorna mensaje esperado con acción pendiente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Procesar mensaje con acción pendiente retorna mensaje")
    void testProcessMessageWithPendingActionReturnsExpectedMessage() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Discount deleted")
                .build();
        when(functionExecutor.executePendingAction(any(), any())).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("confirm")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertEquals("Discount deleted", response.getMessage());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que detecta inglés correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Detecta idioma inglés")
    void testProcessMessageEnglishMessageDetectsEnglish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Operation completed")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("en"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("please confirm this action")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que detecta español correctamente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Detecta idioma español")
    void testProcessMessageSpanishMessageDetectsSpanish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Operación completada")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("es"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("por favor confirmar esta acción")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que detecta español por caracteres especiales")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Detecta español por caracteres especiales")
    void testProcessMessageSpanishCharactersDetectsSpanish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Operación completada")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("es"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("sí, elimínalo")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que mensaje vacío usa español por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Mensaje vacío usa español por defecto")
    void testProcessMessageEmptyMessageDefaultsToSpanish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Done")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("es"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que mensaje null usa español por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Mensaje null usa español por defecto")
    void testProcessMessageNullMessageDefaultsToSpanish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Done")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("es"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message(null)
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Historial de Chat")
    @Description("Verifica que procesa mensaje con historial")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Procesa mensaje con historial")
    void testProcessMessageWithHistoryReturnsSuccess() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Done")
                .build();
        when(functionExecutor.executePendingAction(any(), any())).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest.ChatHistoryEntry historyEntry = ChatRequest.ChatHistoryEntry.builder()
                .role("user")
                .content("Previous message")
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes")
                .history(List.of(historyEntry))
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Historial de Chat")
    @Description("Verifica que procesa historial del modelo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Procesa historial del modelo")
    void testProcessMessageModelHistoryReturnsSuccess() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Done")
                .build();
        when(functionExecutor.executePendingAction(any(), any())).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest.ChatHistoryEntry modelHistoryEntry = ChatRequest.ChatHistoryEntry.builder()
                .role("model")
                .content("Previous model response")
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes")
                .history(List.of(modelHistoryEntry))
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica detección de idioma en mensajes mixtos")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Detecta idioma en mensajes mixtos")
    void testProcessMessageMixedEnglishSpanishDetectsLanguage() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Done")
                .build();
        when(functionExecutor.executePendingAction(any(), any())).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes delete this please")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que keyword create detecta inglés")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Keyword create detecta inglés")
    void testProcessMessageCreateKeywordDetectsEnglish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Created")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("en"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("create a new discount")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Detección de Idioma")
    @Description("Verifica que keyword crear detecta español")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Keyword crear detecta español")
    void testProcessMessageCrearKeywordDetectsSpanish() {
        ChatResponse expectedResponse = ChatResponse.builder()
                .success(true)
                .message("Creado")
                .build();
        when(functionExecutor.executePendingAction(any(), eq("es"))).thenReturn(expectedResponse);

        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("crear un nuevo descuento")
                .pendingAction(pending)
                .build();

        ChatResponse response = geminiService.processMessage(request);
        assertTrue(response.isSuccess());
    }
}

