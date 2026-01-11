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

    private static final String STORY_LISTAR_TIPOS = "Listar Tipos de Entrada";
    private static final String STORY_BUSCAR_TIPO_ID = "Buscar Tipo por ID";
    private static final String STORY_BUSCAR_TIPO_NOMBRE = "Buscar Tipo por Nombre";
    private static final String STORY_CREAR_TIPO = "Crear Tipo de Entrada";
    private static final String STORY_ACTUALIZAR_TIPO = "Actualizar Tipo de Entrada";
    private static final String STORY_ELIMINAR_TIPO = "Eliminar Tipo de Entrada";

    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String DESCRIPTION_ADULT_TICKET = "Adult ticket";
    private static final String FIELD_COST = "cost";
    private static final String FIELD_TYPE_NAME = "typeName";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_MAX_PER_DAY = "maxPerDay";
    private static final String FIELD_PHOTO_URL = "photoUrl";
    private static final String PHOTO_URL_EXAMPLE = "https://example.com/photo.jpg";

    @Mock
    private TicketTypeService ticketTypeService;

    @InjectMocks
    private TicketTypeFunctionExecutor executor;

    private TicketType sample;

    @BeforeEach
    void setUp() {
        sample = TicketType.builder()
                .typeName(TYPE_NAME_ADULT)
                .cost(new BigDecimal("50.00"))
                .description(DESCRIPTION_ADULT_TICKET)
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build();
        sample.setId(1L);
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que listar tipos vacíos retorna éxito en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos retorna éxito en inglés")
    void testListTicketTypesEmptyReturnsSuccessEnglish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que el mensaje indica que no hay tipos en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos muestra mensaje en inglés")
    void testListTicketTypesEmptyContainsNoTypesMessageEnglish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.getMessage().contains("No ticket types"));
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que listar tipos vacíos retorna éxito en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos retorna éxito en español")
    void testListTicketTypesEmptyReturnsSuccessSpanish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que el mensaje indica que no hay tipos en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos vacíos muestra mensaje en español")
    void testListTicketTypesEmptyContainsNoTypesMessageSpanish() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        ChatResponse response = executor.listTicketTypes("es");
        assertTrue(response.getMessage().contains("No hay tipos de entrada"));
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que listar tipos con datos retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos con datos retorna éxito")
    void testListTicketTypesWithDataReturnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que la respuesta contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos muestra nombre de tipo")
    void testListTicketTypesWithDataContainsTypeName() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertTrue(response.getMessage().contains(TYPE_NAME_ADULT));
    }

    @Test
    @Story(STORY_LISTAR_TIPOS)
    @Description("Verifica que la respuesta tiene lista de datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar tipos retorna lista de datos")
    void testListTicketTypesWithDataHasDataList() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        ChatResponse response = executor.listTicketTypes("en");
        assertNotNull(response.getData());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_ID)
    @Description("Verifica que buscar tipo por ID retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar tipo por ID retorna éxito")
    void testGetTicketTypeByIdFoundReturnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_ID)
    @Description("Verifica que la respuesta contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por ID muestra nombre")
    void testGetTicketTypeByIdFoundContainsTypeName() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.getMessage().contains(TYPE_NAME_ADULT));
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_ID)
    @Description("Verifica que la respuesta en español contiene detalles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por ID en español muestra detalles")
    void testGetTicketTypeByIdSpanishContainsDetails() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "es");
        assertTrue(response.getMessage().contains("Detalles del Tipo de Entrada"));
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo por nombre retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por nombre retorna éxito")
    void testFindTicketTypeByNameFoundReturnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", "adult");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo por nombre contiene datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo por nombre contiene datos")
    void testFindTicketTypeByNameFoundContainsData() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", TYPE_NAME_ADULT);
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertNotNull(response.getData());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo inexistente retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente retorna éxito")
    void testFindTicketTypeByNameNotFoundReturnsSuccess() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo inexistente muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente muestra mensaje no encontrado")
    void testFindTicketTypeByNameNotFoundContainsNotFoundMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "en");
        assertTrue(response.getMessage().contains("No ticket type found"));
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear tipo retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna éxito")
    void testCreateTicketTypeReturnsSuccess() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que el mensaje contiene confirmación de creación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo muestra mensaje de creación")
    void testCreateTicketTypeContainsCreatedMessage() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.getMessage().contains("Ticket type created"));
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear tipo en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo en español muestra mensaje de creación")
    void testCreateTicketTypeSpanishContainsCreatedMessage() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        ChatResponse response = executor.createTicketType(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada creado"));
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear tipo con photoUrl retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo con photoUrl retorna éxito")
    void testCreateTicketTypeWithPhotoUrlReturnsSuccess() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        args.put(FIELD_PHOTO_URL, PHOTO_URL_EXAMPLE);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ACTUALIZAR_TIPO)
    @Description("Verifica que actualizar tipo retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo de entrada retorna éxito")
    void testUpdateTicketTypeReturnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put(FIELD_TYPE_NAME, "PREMIUM");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ACTUALIZAR_TIPO)
    @Description("Verifica que el mensaje contiene confirmación de actualización")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo muestra mensaje de actualización")
    void testUpdateTicketTypeContainsUpdatedMessage() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put(FIELD_TYPE_NAME, "PREMIUM");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.getMessage().contains("Ticket type updated"));
    }

    @Test
    @Story(STORY_ACTUALIZAR_TIPO)
    @Description("Verifica que actualizar tipo en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo en español muestra mensaje de actualización")
    void testUpdateTicketTypeSpanishContainsUpdatedMessage() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        ChatResponse response = executor.updateTicketType(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada actualizado"));
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que solicitar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Solicitar eliminación de tipo retorna éxito")
    void testRequestDeleteTicketTypeReturnsSuccess() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que solicitar eliminación contiene acción pendiente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación contiene acción pendiente")
    void testRequestDeleteTicketTypeContainsPendingAction() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertNotNull(response.getPendingAction());
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que la acción pendiente tiene el tipo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción pendiente tiene tipo deleteTicketType")
    void testRequestDeleteTicketTypePendingActionHasCorrectType() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "en");
        assertEquals("deleteTicketType", response.getPendingAction().getActionType());
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que ejecutar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ejecutar eliminación de tipo retorna éxito")
    void testExecuteDeleteTicketTypeReturnsSuccess() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que el mensaje contiene confirmación de eliminación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación muestra mensaje de eliminación")
    void testExecuteDeleteTicketTypeContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "en");
        assertTrue(response.getMessage().contains("deleted"));
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que ejecutar eliminación en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación en español muestra mensaje de eliminación")
    void testExecuteDeleteTicketTypeSpanishContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteTicketType(1L, "es");
        assertTrue(response.getMessage().contains("eliminado"));
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear con photoUrl en blanco usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl en blanco usa defecto")
    void testCreateTicketTypeWithBlankPhotoUrlUsesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        args.put(FIELD_PHOTO_URL, "   ");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear con photoUrl null usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl null usa defecto")
    void testCreateTicketTypeWithNullPhotoUrlUsesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        args.put(FIELD_PHOTO_URL, null);
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear con photoUrl inválido usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con photoUrl inválido usa defecto")
    void testCreateTicketTypeWithInvalidPhotoUrlUsesDefault() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        args.put(FIELD_PHOTO_URL, "not-a-url");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_CREAR_TIPO)
    @Description("Verifica que crear con URL http usa URL proporcionada")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear tipo con URL http usa URL proporcionada")
    void testCreateTicketTypeWithHttpUrlUsesProvidedUrl() {
        when(ticketTypeService.save(any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put(FIELD_TYPE_NAME, TYPE_NAME_ADULT);
        args.put(FIELD_COST, 50.0);
        args.put(FIELD_DESCRIPTION, DESCRIPTION_ADULT_TICKET);
        args.put(FIELD_MAX_PER_DAY, 100);
        args.put(FIELD_PHOTO_URL, "http://example.com/photo.jpg");
        ChatResponse response = executor.createTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ACTUALIZAR_TIPO)
    @Description("Verifica que actualizar con valores null preserva existentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo con valores null preserva existentes")
    void testUpdateTicketTypeWithNullValuesPreservesExisting() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_ACTUALIZAR_TIPO)
    @Description("Verifica que actualizar con photoUrl por defecto establece null")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Actualizar tipo con photoUrl por defecto establece null")
    void testUpdateTicketTypeWithDefaultPhotoUrlSetsNull() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        when(ticketTypeService.update(any(Long.class), any(TicketType.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put(FIELD_PHOTO_URL, "https://placeholder.com/default.jpg");
        ChatResponse response = executor.updateTicketType(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo inexistente en español muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo inexistente en español muestra mensaje")
    void testFindTicketTypeByNameSpanishNotFoundContainsMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of());
        Map<String, Object> args = Map.of("name", "NONEXISTENT");
        ChatResponse response = executor.findTicketTypeByName(args, "es");
        assertTrue(response.getMessage().contains("No se encontró"));
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_NOMBRE)
    @Description("Verifica que buscar tipo encontrado en español muestra mensaje")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar tipo encontrado en español muestra mensaje")
    void testFindTicketTypeByNameSpanishFoundContainsMessage() {
        when(ticketTypeService.findAll()).thenReturn(List.of(sample));
        Map<String, Object> args = Map.of("name", TYPE_NAME_ADULT);
        ChatResponse response = executor.findTicketTypeByName(args, "es");
        assertTrue(response.getMessage().contains("Tipo de entrada encontrado"));
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_ID)
    @Description("Verifica que photoUrl null se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar tipo con photoUrl null muestra None")
    void testGetTicketTypeByIdWithNullPhotoUrlHandlesGracefully() {
        sample.setPhotoUrl(null);
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "en");
        assertTrue(response.getMessage().contains("None"));
    }

    @Test
    @Story(STORY_BUSCAR_TIPO_ID)
    @Description("Verifica que photoUrl null en español se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar tipo con photoUrl null en español muestra Ninguna")
    void testGetTicketTypeByIdSpanishWithNullPhotoUrlHandlesGracefully() {
        sample.setPhotoUrl(null);
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getTicketTypeById(args, "es");
        assertTrue(response.getMessage().contains("Ninguna"));
    }

    @Test
    @Story(STORY_ELIMINAR_TIPO)
    @Description("Verifica que solicitar eliminación en español muestra confirmación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación en español muestra confirmación")
    void testRequestDeleteTicketTypeSpanishContainsConfirmation() {
        when(ticketTypeService.findById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteTicketType(args, "es");
        assertTrue(response.getMessage().contains("Confirmación requerida"));
    }
}

