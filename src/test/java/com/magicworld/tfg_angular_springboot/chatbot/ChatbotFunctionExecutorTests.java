package com.magicworld.tfg_angular_springboot.chatbot;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.chatbot.dto.PendingAction;
import com.magicworld.tfg_angular_springboot.chatbot.executor.AttractionFunctionExecutor;
import com.magicworld.tfg_angular_springboot.chatbot.executor.DiscountFunctionExecutor;
import com.magicworld.tfg_angular_springboot.chatbot.executor.TicketTypeFunctionExecutor;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Ejecutor de Funciones")
public class ChatbotFunctionExecutorTests {

    @Mock
    private DiscountFunctionExecutor discountExecutor;

    @Mock
    private TicketTypeFunctionExecutor ticketTypeExecutor;

    @Mock
    private AttractionFunctionExecutor attractionExecutor;

    @InjectMocks
    private ChatbotFunctionExecutor executor;

    private ChatResponse successResponse;

    @BeforeEach
    void setUp() {
        successResponse = ChatResponse.builder()
                .success(true)
                .message("Success")
                .build();
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que listDiscounts retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listDiscounts retorna éxito")
    void testExecuteFunction_listDiscounts_returnsSuccess() {
        when(discountExecutor.listDiscounts("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listDiscounts", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que getDiscountById retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getDiscountById retorna éxito")
    void testExecuteFunction_getDiscountById_returnsSuccess() {
        when(discountExecutor.getDiscountById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getDiscountById", Map.of("id", 1L), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que createDiscount retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createDiscount retorna éxito")
    void testExecuteFunction_createDiscount_returnsSuccess() {
        when(discountExecutor.createDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que updateDiscount retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateDiscount retorna éxito")
    void testExecuteFunction_updateDiscount_returnsSuccess() {
        when(discountExecutor.updateDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Descuentos")
    @Description("Verifica que requestDeleteDiscount retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteDiscount retorna éxito")
    void testExecuteFunction_requestDeleteDiscount_returnsSuccess() {
        when(discountExecutor.requestDeleteDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que listTicketTypes retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listTicketTypes retorna éxito")
    void testExecuteFunction_listTicketTypes_returnsSuccess() {
        when(ticketTypeExecutor.listTicketTypes("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listTicketTypes", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que getTicketTypeById retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getTicketTypeById retorna éxito")
    void testExecuteFunction_getTicketTypeById_returnsSuccess() {
        when(ticketTypeExecutor.getTicketTypeById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getTicketTypeById", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que findTicketTypeByName retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findTicketTypeByName retorna éxito")
    void testExecuteFunction_findTicketTypeByName_returnsSuccess() {
        when(ticketTypeExecutor.findTicketTypeByName(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("findTicketTypeByName", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que createTicketType retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createTicketType retorna éxito")
    void testExecuteFunction_createTicketType_returnsSuccess() {
        when(ticketTypeExecutor.createTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que updateTicketType retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateTicketType retorna éxito")
    void testExecuteFunction_updateTicketType_returnsSuccess() {
        when(ticketTypeExecutor.updateTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Tipos de Entrada")
    @Description("Verifica que requestDeleteTicketType retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteTicketType retorna éxito")
    void testExecuteFunction_requestDeleteTicketType_returnsSuccess() {
        when(ticketTypeExecutor.requestDeleteTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que listAttractions retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listAttractions retorna éxito")
    void testExecuteFunction_listAttractions_returnsSuccess() {
        when(attractionExecutor.listAttractions("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listAttractions", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que getAttractionById retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAttractionById retorna éxito")
    void testExecuteFunction_getAttractionById_returnsSuccess() {
        when(attractionExecutor.getAttractionById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getAttractionById", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que createAttraction retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createAttraction retorna éxito")
    void testExecuteFunction_createAttraction_returnsSuccess() {
        when(attractionExecutor.createAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que updateAttraction retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateAttraction retorna éxito")
    void testExecuteFunction_updateAttraction_returnsSuccess() {
        when(attractionExecutor.updateAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Funciones de Atracciones")
    @Description("Verifica que requestDeleteAttraction retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteAttraction retorna éxito")
    void testExecuteFunction_requestDeleteAttraction_returnsSuccess() {
        when(attractionExecutor.requestDeleteAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que función desconocida retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Función desconocida retorna error")
    void testExecuteFunction_unknownFunction_returnsError() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que función desconocida contiene mensaje de error")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Función desconocida contiene mensaje")
    void testExecuteFunction_unknownFunction_containsErrorMessage() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "en");
        assertTrue(response.getMessage().contains("Unknown function"));
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica mensaje de error para función desconocida en español")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Función desconocida mensaje español")
    void testExecuteFunction_unknownFunction_spanish() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "es");
        assertTrue(response.getMessage().contains("Función no reconocida"));
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que DateTimeParseException retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DateTimeParseException retorna error")
    void testExecuteFunction_dateTimeParseException_returnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new DateTimeParseException("Invalid date", "invalid", 0));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que DateTimeParseException contiene mensaje de fecha")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("DateTimeParseException contiene mensaje fecha")
    void testExecuteFunction_dateTimeParseException_containsDateMessage() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new DateTimeParseException("Invalid date", "invalid", 0));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertTrue(response.getMessage().contains("YYYY-MM-DD"));
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que IllegalArgumentException retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("IllegalArgumentException retorna error")
    void testExecuteFunction_illegalArgumentException_returnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new IllegalArgumentException("Validation error"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica que excepción genérica retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Excepción genérica retorna error")
    void testExecuteFunction_genericException_returnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new RuntimeException("Unexpected error"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica que deleteDiscount confirmado retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteDiscount confirmado retorna éxito")
    void testExecutePendingAction_confirmed_deleteDiscount_returnsSuccess() {
        when(discountExecutor.executeDeleteDiscount(1L, "en")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica que deleteTicketType confirmado retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteTicketType confirmado retorna éxito")
    void testExecutePendingAction_confirmed_deleteTicketType_returnsSuccess() {
        when(ticketTypeExecutor.executeDeleteTicketType(1L, "en")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteTicketType")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("confirm")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica que deleteAttraction confirmado retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteAttraction confirmado retorna éxito")
    void testExecutePendingAction_confirmed_deleteAttraction_returnsSuccess() {
        when(attractionExecutor.executeDeleteAttraction(1L, "en")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteAttraction")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("ok")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica que acción cancelada retorna mensaje de cancelación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción cancelada retorna mensaje cancelación")
    void testExecutePendingAction_cancelled_returnsCancelledMessage() {
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("no")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.getMessage().contains("cancelled"));
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica mensaje de cancelación en español")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Cancelación mensaje español")
    void testExecutePendingAction_cancelled_spanish() {
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("no")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.getMessage().contains("cancelada"));
    }

    @Test
    @Story("Acciones Pendientes")
    @Description("Verifica que acción desconocida retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción desconocida retorna error")
    void testExecutePendingAction_unknownAction_returnsError() {
        PendingAction pending = PendingAction.builder()
                .actionType("unknownAction")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("yes")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Confirmación en Español")
    @Description("Verifica confirmación con 'sí'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmación con 'sí'")
    void testExecutePendingAction_spanishConfirmation_sí() {
        when(discountExecutor.executeDeleteDiscount(1L, "es")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("sí")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Confirmación en Español")
    @Description("Verifica confirmación con 'confirmo'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmación con 'confirmo'")
    void testExecutePendingAction_spanishConfirmation_confirmo() {
        when(discountExecutor.executeDeleteDiscount(1L, "es")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("confirmo")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Confirmación en Español")
    @Description("Verifica confirmación con 'adelante'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmación con 'adelante'")
    void testExecutePendingAction_spanishConfirmation_adelante() {
        when(discountExecutor.executeDeleteDiscount(1L, "es")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("adelante")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica IllegalArgumentException en español")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("IllegalArgumentException español")
    void testExecuteFunction_illegalArgumentException_spanish() {
        when(discountExecutor.createDiscount(any(), eq("es")))
                .thenThrow(new IllegalArgumentException("Error de validación"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "es");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica excepción genérica en español")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Excepción genérica español")
    void testExecuteFunction_genericException_spanish() {
        when(discountExecutor.createDiscount(any(), eq("es")))
                .thenThrow(new RuntimeException("Error inesperado"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "es");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Confirmación en Inglés")
    @Description("Verifica confirmación con 'proceed'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmación con 'proceed'")
    void testExecutePendingAction_englishConfirmation_proceed() {
        when(discountExecutor.executeDeleteDiscount(1L, "en")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("proceed")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Denegación en Español")
    @Description("Verifica denegación con 'cancelar'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Denegación con 'cancelar'")
    void testExecutePendingAction_spanishDenial_cancelar() {
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("cancelar")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.getMessage().contains("cancelada"));
    }

    @Test
    @Story("Denegación en Inglés")
    @Description("Verifica denegación con 'cancel'")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Denegación con 'cancel'")
    void testExecutePendingAction_englishDenial_cancel() {
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("cancel")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "en");
        assertTrue(response.getMessage().contains("cancelled"));
    }
}

