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

    private static final String TEST_RIDE_NAME = "Test Ride";
    private static final String UPDATED_RIDE_NAME = "Updated Ride";
    private static final String TEST_DESCRIPTION = "A fun ride";
    private static final String UPDATED_DESCRIPTION = "An updated ride";
    private static final String TEST_PHOTO_URL = "http://example.com/photo.jpg";
    private static final String UPDATED_PHOTO_URL = "http://example.com/updated.jpg";
    private static final int TEST_MIN_HEIGHT = 100;
    private static final int TEST_MIN_AGE = 8;
    private static final int TEST_MIN_WEIGHT = 30;
    private static final int UPDATED_MIN_HEIGHT = 120;
    private static final int UPDATED_MIN_AGE = 12;
    private static final int UPDATED_MIN_WEIGHT = 40;

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AttractionRepository attractionRepository;

    private Attraction sample;

    @BeforeEach
    public void setUp() {
        attractionRepository.deleteAll();
        sample = Attraction.builder()
                .name(TEST_RIDE_NAME)
                .intensity(Intensity.MEDIUM)
                .category(AttractionCategory.ROLLER_COASTER)
                .minimumHeight(TEST_MIN_HEIGHT)
                .minimumAge(TEST_MIN_AGE)
                .minimumWeight(TEST_MIN_WEIGHT)
                .description(TEST_DESCRIPTION)
                .photoUrl(TEST_PHOTO_URL)
                .isActive(true)
                .mapPositionX(50.0)
                .mapPositionY(50.0)
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
        assertEquals(TEST_RIDE_NAME, saved.getName());
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
    public void testGetAttractionByIdExists() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction found = attractionService.getAttractionById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @Story("Buscar Atracción por ID")
    @Description("Verifica que se lanza excepción cuando no existe la atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar atracción por ID inexistente lanza excepción")
    public void testGetAttractionByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> attractionService.getAttractionById(999L));
    }

    private Attraction buildUpdateAttraction() {
        return Attraction.builder()
                .name(UPDATED_RIDE_NAME)
                .intensity(Intensity.HIGH)
                .category(AttractionCategory.FERRIS_WHEEL)
                .minimumHeight(UPDATED_MIN_HEIGHT)
                .minimumAge(UPDATED_MIN_AGE)
                .minimumWeight(UPDATED_MIN_WEIGHT)
                .description(UPDATED_DESCRIPTION)
                .photoUrl(UPDATED_PHOTO_URL)
                .isActive(false)
                .mapPositionX(60.0)
                .mapPositionY(60.0)
                .build();
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar el nombre de una atracción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción cambia el nombre")
    public void testUpdateAttractionUpdatesName() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = buildUpdateAttraction();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(UPDATED_RIDE_NAME, updated.getName());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la intensidad de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la intensidad")
    public void testUpdateAttractionUpdatesIntensity() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = buildUpdateAttraction();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(Intensity.HIGH, updated.getIntensity());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la altura mínima de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la altura mínima")
    public void testUpdateAttractionUpdatesMinimumHeight() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = buildUpdateAttraction();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(UPDATED_MIN_HEIGHT, updated.getMinimumHeight());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar la descripción de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia la descripción")
    public void testUpdateAttractionUpdatesDescription() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = buildUpdateAttraction();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(UPDATED_DESCRIPTION, updated.getDescription());
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que se puede actualizar el estado activo de una atracción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción cambia el estado activo")
    public void testUpdateAttractionUpdatesIsActive() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = buildUpdateAttraction();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertFalse(updated.getIsActive());
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se pueden filtrar atracciones con filtros válidos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar atracciones con parámetros válidos retorna resultados")
    public void testGetAllAttractionsWithFiltersValidFilters() {
        attractionService.saveAttraction(sample);
        List<Attraction> result = attractionService.getAllAttractions(150, 50, 10);
        assertEquals(1, result.size());
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con altura mínima negativa")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con altura negativa lanza excepción")
    public void testGetAllAttractionsWithFiltersNegativeMinHeight() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(-1, null, null));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con peso mínimo negativo")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con peso negativo lanza excepción")
    public void testGetAllAttractionsWithFiltersNegativeMinWeight() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(null, -1, null));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que se lanza excepción con edad mínima negativa")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Filtrar con edad negativa lanza excepción")
    public void testGetAllAttractionsWithFiltersNegativeMinAge() {
        assertThrows(BadRequestException.class, () -> attractionService.getAllAttractions(null, null, -1));
    }

    @Test
    @Story("Actualizar Atracción")
    @Description("Verifica que al actualizar con photoUrl null se mantiene la existente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar con photoUrl null mantiene URL existente")
    public void testUpdateAttractionWithNullPhotoUrlKeepsExisting() {
        Attraction saved = attractionService.saveAttraction(sample);
        Attraction update = Attraction.builder()
                .name(UPDATED_RIDE_NAME)
                .intensity(Intensity.HIGH)
                .category(AttractionCategory.FERRIS_WHEEL)
                .minimumHeight(UPDATED_MIN_HEIGHT)
                .minimumAge(UPDATED_MIN_AGE)
                .minimumWeight(UPDATED_MIN_WEIGHT)
                .description(UPDATED_DESCRIPTION)
                .photoUrl(null)
                .isActive(false)
                .mapPositionX(60.0)
                .mapPositionY(60.0)
                .build();
        Attraction updated = attractionService.updateAttraction(saved.getId(), update);
        assertEquals(TEST_PHOTO_URL, updated.getPhotoUrl());
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
