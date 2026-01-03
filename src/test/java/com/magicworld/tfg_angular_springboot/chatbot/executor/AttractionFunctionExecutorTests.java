package com.magicworld.tfg_angular_springboot.chatbot.executor;

import com.magicworld.tfg_angular_springboot.attraction.Attraction;
import com.magicworld.tfg_angular_springboot.attraction.AttractionService;
import com.magicworld.tfg_angular_springboot.attraction.Intensity;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Epic("Chatbot IA")
@Feature("Ejecutor de Funciones de Atracciones")
public class AttractionFunctionExecutorTests {

    @Mock
    private AttractionService attractionService;

    @InjectMocks
    private AttractionFunctionExecutor executor;

    private Attraction sample;

    @BeforeEach
    void setUp() {
        sample = Attraction.builder()
                .name("Roller Coaster")
                .intensity(Intensity.HIGH)
                .minimumHeight(140)
                .minimumAge(12)
                .minimumWeight(30)
                .description("Extreme roller coaster")
                .photoUrl("https://example.com/coaster.jpg")
                .isActive(true)
                .build();
        sample.setId(1L);
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que listar atracciones vacías retorna éxito en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones vacías retorna éxito en inglés")
    void testListAttractionsEmptyReturnsSuccessEnglish() {
        when(attractionService.getAllAttractions()).thenReturn(List.of());
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que el mensaje indica que no hay atracciones en inglés")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones vacías muestra mensaje en inglés")
    void testListAttractionsEmptyContainsNoAttractionsMessageEnglish() {
        when(attractionService.getAllAttractions()).thenReturn(List.of());
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.getMessage().contains("No attractions"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que listar atracciones vacías retorna éxito en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones vacías retorna éxito en español")
    void testListAttractionsEmptyReturnsSuccessSpanish() {
        when(attractionService.getAllAttractions()).thenReturn(List.of());
        ChatResponse response = executor.listAttractions("es");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que el mensaje indica que no hay atracciones en español")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones vacías muestra mensaje en español")
    void testListAttractionsEmptyContainsNoAttractionsMessageSpanish() {
        when(attractionService.getAllAttractions()).thenReturn(List.of());
        ChatResponse response = executor.listAttractions("es");
        assertTrue(response.getMessage().contains("No hay atracciones"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que listar atracciones con datos retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones con datos retorna éxito")
    void testListAttractionsWithDataReturnsSuccess() {
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que la respuesta contiene el nombre de la atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones muestra nombre de atracción")
    void testListAttractionsWithDataContainsAttractionName() {
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.getMessage().contains("Roller Coaster"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que la respuesta tiene lista de datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar atracciones retorna lista de datos")
    void testListAttractionsWithDataHasDataList() {
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("en");
        assertNotNull(response.getData());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que atracción activa muestra estado activo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Atracción activa muestra estado Active")
    void testListAttractionsActiveAttractionShowsActiveStatus() {
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.getMessage().contains("Active"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que atracción inactiva muestra estado inactivo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Atracción inactiva muestra estado Inactive")
    void testListAttractionsInactiveAttractionShowsInactiveStatus() {
        sample.setIsActive(false);
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("en");
        assertTrue(response.getMessage().contains("Inactive"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que buscar atracción por ID retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar atracción por ID retorna éxito")
    void testGetAttractionByIdFoundReturnsSuccess() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que la respuesta contiene el nombre de la atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar atracción por ID muestra nombre")
    void testGetAttractionByIdFoundContainsAttractionName() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "en");
        assertTrue(response.getMessage().contains("Roller Coaster"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que la respuesta en español contiene detalles")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar atracción por ID en español muestra detalles")
    void testGetAttractionByIdSpanishContainsDetails() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "es");
        assertTrue(response.getMessage().contains("Detalles de la Atracción"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que la respuesta contiene la intensidad")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar atracción por ID muestra intensidad")
    void testGetAttractionByIdContainsIntensity() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "en");
        assertTrue(response.getMessage().contains("HIGH"));
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción retorna éxito")
    void testCreateAttractionReturnsSuccess() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que el mensaje contiene confirmación de creación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción muestra mensaje de creación")
    void testCreateAttractionContainsCreatedMessage() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.getMessage().contains("Attraction created"));
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción en español muestra mensaje de creación")
    void testCreateAttractionSpanishContainsCreatedMessage() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        ChatResponse response = executor.createAttraction(args, "es");
        assertTrue(response.getMessage().contains("Atracción creada"));
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción con photoUrl retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción con photoUrl retorna éxito")
    void testCreateAttractionWithPhotoUrlReturnsSuccess() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("photoUrl", "https://example.com/photo.jpg");
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción con isActive retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción con isActive retorna éxito")
    void testCreateAttractionWithIsActiveReturnsSuccess() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("isActive", false);
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción retorna éxito")
    void testUpdateAttractionReturnsSuccess() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("name", "Super Coaster");
        ChatResponse response = executor.updateAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que el mensaje contiene confirmación de actualización")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción muestra mensaje de actualización")
    void testUpdateAttractionContainsUpdatedMessage() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("name", "Super Coaster");
        ChatResponse response = executor.updateAttraction(args, "en");
        assertTrue(response.getMessage().contains("Attraction updated"));
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción en español muestra mensaje de actualización")
    void testUpdateAttractionSpanishContainsUpdatedMessage() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        ChatResponse response = executor.updateAttraction(args, "es");
        assertTrue(response.getMessage().contains("Atracción actualizada"));
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar intensidad retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar intensidad de atracción retorna éxito")
    void testUpdateAttractionUpdatesIntensity() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("intensity", "LOW");
        ChatResponse response = executor.updateAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que solicitar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Solicitar eliminación de atracción retorna éxito")
    void testRequestDeleteAttractionReturnsSuccess() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que solicitar eliminación contiene acción pendiente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación contiene acción pendiente")
    void testRequestDeleteAttractionContainsPendingAction() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteAttraction(args, "en");
        assertNotNull(response.getPendingAction());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que la acción pendiente tiene el tipo correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Acción pendiente tiene tipo deleteAttraction")
    void testRequestDeleteAttractionPendingActionHasCorrectType() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteAttraction(args, "en");
        assertEquals("deleteAttraction", response.getPendingAction().getActionType());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que ejecutar eliminación retorna éxito")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ejecutar eliminación de atracción retorna éxito")
    void testExecuteDeleteAttractionReturnsSuccess() {
        ChatResponse response = executor.executeDeleteAttraction(1L, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que el mensaje contiene confirmación de eliminación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación muestra mensaje de eliminación")
    void testExecuteDeleteAttractionContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteAttraction(1L, "en");
        assertTrue(response.getMessage().contains("deleted"));
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que ejecutar eliminación en español muestra mensaje correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ejecutar eliminación en español muestra mensaje de eliminación")
    void testExecuteDeleteAttractionSpanishContainsDeletedMessage() {
        ChatResponse response = executor.executeDeleteAttraction(1L, "es");
        assertTrue(response.getMessage().contains("eliminada"));
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear con photoUrl en blanco usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear atracción con photoUrl en blanco usa defecto")
    void testCreateAttractionWithBlankPhotoUrlUsesDefault() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("photoUrl", "   ");
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear con photoUrl null usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear atracción con photoUrl null usa defecto")
    void testCreateAttractionWithNullPhotoUrlUsesDefault() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("photoUrl", null);
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear con photoUrl inválido usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear atracción con photoUrl inválido usa defecto")
    void testCreateAttractionWithInvalidPhotoUrlUsesDefault() {
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("photoUrl", "not-a-valid-url");
        ChatResponse response = executor.createAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar con todos los campos retorna éxito")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción con todos los campos retorna éxito")
    void testUpdateAttractionWithAllFieldsReturnsSuccess() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("name", "Super Coaster");
        args.put("intensity", "LOW");
        args.put("minimumHeight", 100);
        args.put("minimumAge", 8);
        args.put("minimumWeight", 25);
        args.put("description", "Updated description");
        args.put("isActive", false);
        args.put("photoUrl", "https://example.com/new.jpg");
        ChatResponse response = executor.updateAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar con photoUrl por defecto establece null")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Actualizar atracción con photoUrl por defecto establece null")
    void testUpdateAttractionWithDefaultPhotoUrlSetsNull() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        when(attractionService.updateAttraction(any(Long.class), any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("id", 1L);
        args.put("photoUrl", "https://placeholder.com/default.jpg");
        ChatResponse response = executor.updateAttraction(args, "en");
        assertTrue(response.isSuccess());
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que atracción inactiva muestra estado inactivo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar atracción inactiva muestra estado Inactive")
    void testGetAttractionByIdInactiveAttractionShowsInactiveStatus() {
        sample.setIsActive(false);
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "en");
        assertTrue(response.getMessage().contains("Inactive"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que atracción inactiva en español muestra estado inactivo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar atracción inactiva en español muestra estado Inactiva")
    void testGetAttractionByIdSpanishInactiveAttractionShowsInactiveStatus() {
        sample.setIsActive(false);
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "es");
        assertTrue(response.getMessage().contains("Inactiva"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que photoUrl null se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar atracción con photoUrl null muestra None")
    void testGetAttractionByIdWithNullPhotoUrlHandlesGracefully() {
        sample.setPhotoUrl(null);
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "en");
        assertTrue(response.getMessage().contains("None"));
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que photoUrl null en español se maneja correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Buscar atracción con photoUrl null en español muestra Ninguna")
    void testGetAttractionByIdSpanishWithNullPhotoUrlHandlesGracefully() {
        sample.setPhotoUrl(null);
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.getAttractionById(args, "es");
        assertTrue(response.getMessage().contains("Ninguna"));
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que solicitar eliminación en español muestra confirmación")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Solicitar eliminación en español muestra confirmación")
    void testRequestDeleteAttractionSpanishContainsConfirmation() {
        when(attractionService.getAttractionById(1L)).thenReturn(sample);
        Map<String, Object> args = Map.of("id", 1L);
        ChatResponse response = executor.requestDeleteAttraction(args, "es");
        assertTrue(response.getMessage().contains("Confirmación requerida"));
    }

    @Test
    @Story("Crear Atracción")
    @Description("Verifica que crear atracción inactiva en español muestra estado")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear atracción inactiva en español muestra Inactiva")
    void testCreateAttractionSpanishInactiveShowsStatus() {
        sample.setIsActive(false);
        when(attractionService.saveAttraction(any(Attraction.class))).thenReturn(sample);
        Map<String, Object> args = new HashMap<>();
        args.put("name", "Roller Coaster");
        args.put("intensity", "HIGH");
        args.put("minimumHeight", 140);
        args.put("minimumAge", 12);
        args.put("minimumWeight", 30);
        args.put("description", "Extreme roller coaster");
        args.put("isActive", false);
        ChatResponse response = executor.createAttraction(args, "es");
        assertTrue(response.getMessage().contains("Inactiva"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que listar atracciones en español muestra intensidad")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Listar atracciones en español muestra Intensidad")
    void testListAttractionsSpanishShowsIntensity() {
        when(attractionService.getAllAttractions()).thenReturn(List.of(sample));
        ChatResponse response = executor.listAttractions("es");
        assertTrue(response.getMessage().contains("Intensidad"));
    }
}

