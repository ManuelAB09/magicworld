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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Ejecutor de Funciones")
public class ChatbotFunctionExecutorTests {

    private static final String STORY_FUNCIONES_DESCUENTOS = "Funciones de Descuentos";
    private static final String STORY_FUNCIONES_TIPOS_ENTRADA = "Funciones de Tipos de Entrada";
    private static final String STORY_FUNCIONES_ATRACCIONES = "Funciones de Atracciones";
    private static final String STORY_MANEJO_ERRORES = "Manejo de Errores";
    private static final String STORY_ACCIONES_PENDIENTES = "Acciones Pendientes";
    private static final String ACTION_DELETE_DISCOUNT = "deleteDiscount";

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
    @Story(STORY_FUNCIONES_DESCUENTOS)
    @Description("Verifica que listDiscounts retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listDiscounts retorna exito")
    void testExecuteFunctionListDiscountsReturnsSuccess() {
        when(discountExecutor.listDiscounts("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listDiscounts", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_DESCUENTOS)
    @Description("Verifica que getDiscountById retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getDiscountById retorna exito")
    void testExecuteFunctionGetDiscountByIdReturnsSuccess() {
        when(discountExecutor.getDiscountById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getDiscountById", Map.of("id", 1L), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_DESCUENTOS)
    @Description("Verifica que createDiscount retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createDiscount retorna exito")
    void testExecuteFunctionCreateDiscountReturnsSuccess() {
        when(discountExecutor.createDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_DESCUENTOS)
    @Description("Verifica que updateDiscount retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateDiscount retorna exito")
    void testExecuteFunctionUpdateDiscountReturnsSuccess() {
        when(discountExecutor.updateDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_DESCUENTOS)
    @Description("Verifica que requestDeleteDiscount retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteDiscount retorna exito")
    void testExecuteFunctionRequestDeleteDiscountReturnsSuccess() {
        when(discountExecutor.requestDeleteDiscount(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteDiscount", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que listTicketTypes retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listTicketTypes retorna exito")
    void testExecuteFunctionListTicketTypesReturnsSuccess() {
        when(ticketTypeExecutor.listTicketTypes("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listTicketTypes", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que getTicketTypeById retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getTicketTypeById retorna exito")
    void testExecuteFunctionGetTicketTypeByIdReturnsSuccess() {
        when(ticketTypeExecutor.getTicketTypeById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getTicketTypeById", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que findTicketTypeByName retorna exito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("findTicketTypeByName retorna exito")
    void testExecuteFunctionFindTicketTypeByNameReturnsSuccess() {
        when(ticketTypeExecutor.findTicketTypeByName(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("findTicketTypeByName", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que createTicketType retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createTicketType retorna exito")
    void testExecuteFunctionCreateTicketTypeReturnsSuccess() {
        when(ticketTypeExecutor.createTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que updateTicketType retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateTicketType retorna exito")
    void testExecuteFunctionUpdateTicketTypeReturnsSuccess() {
        when(ticketTypeExecutor.updateTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_TIPOS_ENTRADA)
    @Description("Verifica que requestDeleteTicketType retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteTicketType retorna exito")
    void testExecuteFunctionRequestDeleteTicketTypeReturnsSuccess() {
        when(ticketTypeExecutor.requestDeleteTicketType(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteTicketType", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_ATRACCIONES)
    @Description("Verifica que listAttractions retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("listAttractions retorna exito")
    void testExecuteFunctionListAttractionsReturnsSuccess() {
        when(attractionExecutor.listAttractions("en")).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("listAttractions", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_ATRACCIONES)
    @Description("Verifica que getAttractionById retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("getAttractionById retorna exito")
    void testExecuteFunctionGetAttractionByIdReturnsSuccess() {
        when(attractionExecutor.getAttractionById(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("getAttractionById", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_ATRACCIONES)
    @Description("Verifica que createAttraction retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("createAttraction retorna exito")
    void testExecuteFunctionCreateAttractionReturnsSuccess() {
        when(attractionExecutor.createAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("createAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_ATRACCIONES)
    @Description("Verifica que updateAttraction retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("updateAttraction retorna exito")
    void testExecuteFunctionUpdateAttractionReturnsSuccess() {
        when(attractionExecutor.updateAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("updateAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_FUNCIONES_ATRACCIONES)
    @Description("Verifica que requestDeleteAttraction retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("requestDeleteAttraction retorna exito")
    void testExecuteFunctionRequestDeleteAttractionReturnsSuccess() {
        when(attractionExecutor.requestDeleteAttraction(any(), eq("en"))).thenReturn(successResponse);
        ChatResponse response = executor.executeFunction("requestDeleteAttraction", Map.of(), "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que funcion desconocida retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Funcion desconocida retorna error")
    void testExecuteFunctionUnknownFunctionReturnsError() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que funcion desconocida contiene mensaje de error")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Funcion desconocida contiene mensaje")
    void testExecuteFunctionUnknownFunctionContainsErrorMessage() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "en");
        assertTrue(response.getMessage().contains("Unknown function"));
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica mensaje de error para funcion desconocida en espanol")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Funcion desconocida mensaje espanol")
    void testExecuteFunctionUnknownFunctionSpanish() {
        ChatResponse response = executor.executeFunction("unknownFunction", Map.of(), "es");
        assertTrue(response.getMessage().contains("Función no reconocida"));
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que DateTimeParseException retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DateTimeParseException retorna error")
    void testExecuteFunctionDateTimeParseExceptionReturnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new DateTimeParseException("Invalid date", "invalid", 0));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que DateTimeParseException contiene mensaje de fecha")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("DateTimeParseException contiene mensaje fecha")
    void testExecuteFunctionDateTimeParseExceptionContainsDateMessage() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new DateTimeParseException("Invalid date", "invalid", 0));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertTrue(response.getMessage().contains("YYYY-MM-DD"));
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que IllegalArgumentException retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("IllegalArgumentException retorna error")
    void testExecuteFunctionIllegalArgumentExceptionReturnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new IllegalArgumentException("Validation error"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story(STORY_MANEJO_ERRORES)
    @Description("Verifica que excepcion generica retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Excepcion generica retorna error")
    void testExecuteFunctionGenericExceptionReturnsError() {
        when(discountExecutor.createDiscount(any(), eq("en")))
                .thenThrow(new RuntimeException("Unexpected error"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "en");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica que deleteDiscount confirmado retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteDiscount confirmado retorna exito")
    void testExecutePendingActionConfirmedDeleteDiscountReturnsSuccess() {
        when(discountExecutor.executeDeleteDiscount(1L, "en")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType(ACTION_DELETE_DISCOUNT)
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
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica que deleteTicketType confirmado retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteTicketType confirmado retorna exito")
    void testExecutePendingActionConfirmedDeleteTicketTypeReturnsSuccess() {
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
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica que deleteAttraction confirmado retorna exito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("deleteAttraction confirmado retorna exito")
    void testExecutePendingActionConfirmedDeleteAttractionReturnsSuccess() {
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
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica que accion cancelada retorna mensaje de cancelacion")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Accion cancelada retorna mensaje cancelacion")
    void testExecutePendingActionCancelledReturnsCancelledMessage() {
        PendingAction pending = PendingAction.builder()
                .actionType(ACTION_DELETE_DISCOUNT)
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
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica mensaje de cancelacion en espanol")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Cancelacion mensaje espanol")
    void testExecutePendingActionCancelledSpanish() {
        PendingAction pending = PendingAction.builder()
                .actionType(ACTION_DELETE_DISCOUNT)
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
    @Story(STORY_ACCIONES_PENDIENTES)
    @Description("Verifica que accion desconocida retorna error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Accion desconocida retorna error")
    void testExecutePendingActionUnknownActionReturnsError() {
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
    @Story("Confirmacion en Espanol")
    @Description("Verifica confirmacion con si")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmacion con si")
    void testExecutePendingActionSpanishConfirmationSi() {
        when(discountExecutor.executeDeleteDiscount(1L, "es")).thenReturn(successResponse);
        PendingAction pending = PendingAction.builder()
                .actionType("deleteDiscount")
                .params(Map.of("id", 1L))
                .build();
        ChatRequest request = ChatRequest.builder()
                .message("si")
                .pendingAction(pending)
                .build();
        ChatResponse response = executor.executePendingAction(request, "es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Confirmacion en Espanol")
    @Description("Verifica confirmacion con confirmo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmacion con confirmo")
    void testExecutePendingActionSpanishConfirmationConfirmo() {
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
    @Story("Confirmacion en Espanol")
    @Description("Verifica confirmacion con adelante")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmacion con adelante")
    void testExecutePendingActionSpanishConfirmationAdelante() {
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
    @Description("Verifica IllegalArgumentException en espanol")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("IllegalArgumentException espanol")
    void testExecuteFunctionIllegalArgumentExceptionSpanish() {
        when(discountExecutor.createDiscount(any(), eq("es")))
                .thenThrow(new IllegalArgumentException("Error de validacion"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "es");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Manejo de Errores")
    @Description("Verifica excepcion generica en espanol")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Excepcion generica espanol")
    void testExecuteFunctionGenericExceptionSpanish() {
        when(discountExecutor.createDiscount(any(), eq("es")))
                .thenThrow(new RuntimeException("Error inesperado"));
        ChatResponse response = executor.executeFunction("createDiscount", Map.of(), "es");
        assertFalse(response.isSuccess());
    }

    @Test
    @Story("Confirmacion en Ingles")
    @Description("Verifica confirmacion con proceed")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Confirmacion con proceed")
    void testExecutePendingActionEnglishConfirmationProceed() {
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
    @Story("Denegacion en Espanol")
    @Description("Verifica denegacion con cancelar")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Denegacion con cancelar")
    void testExecutePendingActionSpanishDenialCancelar() {
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
    @Story("Denegacion en Ingles")
    @Description("Verifica denegacion con cancel")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Denegacion con cancel")
    void testExecutePendingActionEnglishDenialCancel() {
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

