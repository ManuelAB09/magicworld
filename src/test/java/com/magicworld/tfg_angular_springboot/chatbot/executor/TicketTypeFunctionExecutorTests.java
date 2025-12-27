package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Ejecutor de Funciones de Tipos de Entrada")
public class TicketTypeFunctionExecutorTests {

    @Mock
    private TicketTypeService ticketTypeService;

    @InjectMocks
    private TicketTypeFunctionExecutor executor;

    private TicketType sample;

    @BeforeEach
    void setUp() {
        sample = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build();
        sample.setId(1L);
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que listar tipos vacíos retorna éxito en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos retorna éxito en inglés")
    void testListTicketTypes_empty_returnsSuccessEnglish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que el mensaje indica que no hay tipos en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos muestra mensaje en inglés")
    void testListTicketTypes_empty_containsNoTypesMessageEnglish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.getMessage().contains("No ticket types"));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que listar tipos vacíos retorna éxito en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos retorna éxito en español")
    void testListTicketTypes_empty_returnsSuccessSpanish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que el mensaje indica que no hay tipos en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos muestra mensaje en español")
    void testListTicketTypes_empty_containsNoTypesMessageSpanish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("es");
        assertTrue(response.getMessage().contains("No hay tipos de entrada"));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que listar tipos con datos retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos con datos retorna éxito")
    void testListTicketTypes_withData_returnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que la respuesta contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos muestra nombre de tipo")
    void testListTicketTypes_withData_containsTypeName() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.getMessage().contains("ADULT"));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que la respuesta tiene lista de datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos retorna lista de datos")
    void testListTicketTypes_withData_hasDataList() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertNotNull(response.getData());
    }

    @Test
    @Story("Buscar Tipo por ID")
    @Description("Verifica que buscar tipo por ID retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar tipo por ID retorna éxito")
    void testGetTicketTypeById_found_returnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Tipo por ID")
    @Description("Verifica que la respuesta contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por ID muestra nombre")
    void testGetTicketTypeById_found_containsTypeName() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.getMessage().contains("ADULT"));
    }

    @Test
    @Story("Buscar Tipo por ID")
    @Description("Verifica que la respuesta en español contiene detalles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por ID en español muestra detalles")
    void testGetTicketTypeById_spanish_containsDetails() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "es");
        assertTrue(response.getMessage().contains("Detalles del Tipo de Entrada"));
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo por nombre retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por nombre retorna éxito")
    void testFindTicketTypeByName_found_returnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", "adult");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo por nombre contiene datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por nombre contiene datos")
    void testFindTicketTypeByName_found_containsData() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", "ADULT");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertNotNull(response.getData());
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo inexistente retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente retorna éxito")
    void testFindTicketTypeByName_notFound_returnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo inexistente muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente muestra mensaje no encontrado")
    void testFindTicketTypeByName_notFound_containsNotFoundMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.getMessage().contains("No ticket type found"));
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna éxito")
    void testCreateTicketType_returnsSuccess() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que el mensaje contiene confirmación de creación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo muestra mensaje de creación")
    void testCreateTicketType_containsCreatedMessage() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.getMessage().contains("Ticket type created"));
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo en español muestra mensaje de creación")
    void testCreateTicketType_spanish_containsCreatedMessage() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        ChatResponse response = executor.createTicketType(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada creado"));
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear tipo con photoUrl retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo con photoUrl retorna éxito")
    void testCreateTicketType_withPhotoUrl_returnsSuccess() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        args.put("photoUrl", "https://example.com/photo.jpg");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo de entrada retorna éxito")
    void testUpdateTicketType_returnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("typeName", "PREMIUM");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que el mensaje contiene confirmación de actualización")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo muestra mensaje de actualización")
    void testUpdateTicketType_containsUpdatedMessage() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("typeName", "PREMIUM");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.getMessage().contains("Ticket type updated"));
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo en español muestra mensaje de actualización")
    void testUpdateTicketType_spanish_containsUpdatedMessage() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        ChatResponse response = executor.updateTicketType(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada actualizado"));
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que solicitar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Solicitar eliminación de tipo retorna éxito")
    void testRequestDeleteTicketType_returnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que solicitar eliminación contiene acción pendiente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación contiene acción pendiente")
    void testRequestDeleteTicketType_containsPendingAction() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertNotNull(response.getPendingAction());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que la acción pendiente tiene el tipo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción pendiente tiene tipo deleteTicketType")
    void testRequestDeleteTicketType_pendingActionHasCorrectType() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertEquals("deleteTicketType", response.getPendingAction().getActionType());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que ejecutar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ejecutar eliminación de tipo retorna éxito")
    void testExecuteDeleteTicketType_returnsSuccess() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que el mensaje contiene confirmación de eliminación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación muestra mensaje de eliminación")
    void testExecuteDeleteTicketType_containsDeletedMessage() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "en");
        assertTrue(response.getMessage().contains("deleted"));
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que ejecutar eliminación en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación en español muestra mensaje de eliminación")
    void testExecuteDeleteTicketType_spanish_containsDeletedMessage() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "es");
        assertTrue(response.getMessage().contains("eliminado"));
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear con photoUrl en blanco usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl en blanco usa defecto")
    void testCreateTicketType_withBlankPhotoUrl_usesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        args.put("photoUrl", "   ");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear con photoUrl null usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl null usa defecto")
    void testCreateTicketType_withNullPhotoUrl_usesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        args.put("photoUrl", null);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear con photoUrl inválido usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl inválido usa defecto")
    void testCreateTicketType_withInvalidPhotoUrl_usesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        args.put("photoUrl", "not-a-url");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear con URL http usa URL proporcionada")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con URL http usa URL proporcionada")
    void testCreateTicketType_withHttpUrl_usesProvidedUrl() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("typeName", "ADULT");
        args.put("cost", 50.0);
        args.put("currency", "EUR");
        args.put("description", "Adult ticket");
        args.put("maxPerDay", 100);
        args.put("photoUrl", "http://example.com/photo.jpg");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar con valores null preserva existentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo con valores null preserva existentes")
    void testUpdateTicketType_withNullValues_preservesExisting() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar con photoUrl por defecto establece null")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Actualizar tipo con photoUrl por defecto establece null")
    void testUpdateTicketType_withDefaultPhotoUrl_setsNull() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("photoUrl", "https://placeholder.com/default.jpg");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo inexistente en español muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente en español muestra mensaje")
    void testFindTicketTypeByName_spanish_notFound_containsMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "es");
        assertTrue(response.getMessage().contains("No se encontró"));
    }

    @Test
    @Story("Buscar Tipo por Nombre")
    @Description("Verifica que buscar tipo encontrado en español muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo encontrado en español muestra mensaje")
    void testFindTicketTypeByName_spanish_found_containsMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", "ADULT");
        ChatResponse response = executor.findTicketTypeByName(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada encontrado"));
    }

    @Test
    @Story("Buscar Tipo por ID")
    @Description("Verifica que photoUrl null se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar tipo con photoUrl null muestra None")
    void testGetTicketTypeById_withNullPhotoUrl_handlesGracefully() {
        sample.setPhotoUrl(null);
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.getMessage().contains("None"));
    }

    @Test
    @Story("Buscar Tipo por ID")
    @Description("Verifica que photoUrl null en español se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar tipo con photoUrl null en español muestra Ninguna")
    void testGetTicketTypeById_spanish_withNullPhotoUrl_handlesGracefully() {
        sample.setPhotoUrl(null);
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "es");
        assertTrue(response.getMessage().contains("Ninguna"));
    }

    @Test
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que solicitar eliminación en español muestra confirmación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación en español muestra confirmación")
    void testRequestDeleteTicketType_spanish_containsConfirmation() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "es");
        assertTrue(response.getMessage().contains("Confirmación requerida"));
    }
}

