package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Epic("Gestión de Atracciones")
@Feature("Servicio de Atracciones")
public class AttractionServiceTests {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AttractionRepository attractionRepository;

    private Attraction sample;

    @BeforeEach
    public void setUp() {
        attractionRepository.deleteAll();
        sample = Attraction.builder()
                .name("Test Ride")
                .intensity(Intensity.MEDIUM)
                .minimumHeight(100)
                .minimumAge(8)
                .minimumWeight(30)
                .description("A fun ride")
                .photoUrl("http://example.com/photo.jpg")
                .isActive(true)
                .build();
    }

    @AfterEach
    public void tearDown() {
        attractionRepository.deleteAll();
    }

    @Test
    @Story("Guardar Atracción")
    @Description("Verifica que se puede guardar una atracción y obtener su ID")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Guardar atracción retorna ID generado")
    public void testSaveAttraction() {
        Attraction saved = attractionService.saveAttraction(sample);
        assertNotNull(saved.getId());
        assertEquals("Test Ride", saved.getName());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que se pueden obtener todas las atracciones")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todas las atracciones retorna lista no vacía")
    public void testGetAllAttractions() {
        attractionService.saveAttraction(sample);
        List<Attraction> all = attractionService.getAllAttractions();
        assertFalse(all.isEmpty());
        assertEquals(1, all.size());
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que se puede obtener una atracción existente por su ID")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar atracción por ID existente retorna atracción")
    public void testGetAttractionById_exists() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction found = attractionService.getAttractionById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que se lanza excepción cuando no existe la atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar atracción por ID inexistente lanza excepción")
    public void testGetAttractionById_notFound() {
        assertThrows(ResourceNotFoundException.class, () -> attractionService.getAttractionById(999L));
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar el nombre de una atracción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción cambia el nombre")
    public void testUpdateAttraction_updatesName() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals("Updated Ride", updated.getName());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la intensidad de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la intensidad")
    public void testUpdateAttraction_updatesIntensity() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(Intensity.HIGH, updated.getIntensity());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la altura mínima de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la altura mínima")
    public void testUpdateAttraction_updatesMinimumHeight() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(120, updated.getMinimumHeight());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la descripción de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la descripción")
    public void testUpdateAttraction_updatesDescription() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals("An updated ride", updated.getDescription());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar el estado activo de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia el estado activo")
    public void testUpdateAttraction_updatesIsActive() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl("http://example.com/updated.jpg")
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertFalse(updated.getIsActive());
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se pueden filtrar atracciones con filtros válidos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar atracciones con parámetros válidos retorna resultados")
    public void testGetAllAttractionsWithFilters_validFilters() {
        attractionService.saveAttraction(sample);
        List<Attraction> result = attractionService.getAllAttractions(150, 50, 10);
        assertEquals(1, result.size());
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con altura mínima negativa")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con altura negativa lanza excepción")
    public void testGetAllAttractionsWithFilters_negativeMinHeight() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(-1, null, null));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con peso mínimo negativo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con peso negativo lanza excepción")
    public void testGetAllAttractionsWithFilters_negativeMinWeight() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(null, -1, null));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con edad mínima negativa")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con edad negativa lanza excepción")
    public void testGetAllAttractionsWithFilters_negativeMinAge() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(null, null, -1));
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que al actualizar con photoUrl null se mantiene la existente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar con photoUrl null mantiene URL existente")
    public void testUpdateAttraction_withNullPhotoUrl_keepsExisting() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name("Updated Ride")
                .intensity(Intensity.HIGH)
                .minimumHeight(120)
                .minimumAge(12)
                .minimumWeight(40)
                .description("An updated ride")
                .photoUrl(null)
                .isActive(false)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals("http://example.com/photo.jpg", updated.getPhotoUrl());
    }

    @Test
    @Story("Eliminar Atracción")
    @Description("Verifica que se puede eliminar una atracción existente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar atracción existente la remueve del sistema")
    public void testDeleteAttraction() {
        Attraction saved = attractionService.saveAttraction(sample);
        Long id = saved.getId();
        attractionService.deleteAttraction(id);
        assertThrows(ResourceNotFoundException.class, () -> attractionService.getAttractionById(id));
    }
}
