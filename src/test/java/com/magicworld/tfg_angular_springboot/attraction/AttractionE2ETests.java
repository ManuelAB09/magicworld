package com.magicworld.tfg_angular_springboot.attraction;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Atracciones")
@Feature("API REST de Atracciones E2E")
public class AttractionE2ETests {

    private static final String API_ATTRACTIONS = "/api/v1/attractions";
    private static final String ROLLER_COASTER_NAME = "Roller Coaster";
    private static final String SUPER_COASTER_NAME = "Super Coaster";
    private static final String WATER_SLIDE_NAME = "Water Slide";
    private static final String UPDATED_COASTER_NAME = "Updated Coaster";
    private static final String PHOTO_UPDATED_COASTER_NAME = "Photo Updated Coaster";
    private static final String EXTREME_COASTER_DESC = "Extreme roller coaster";
    private static final String FUN_WATER_SLIDE_DESC = "Fun water slide";
    private static final String UPDATED_DESC = "Updated description";
    private static final String PHOTO_UPDATED_DESC = "Photo updated description";
    private static final String PHOTO_URL = "https://example.com/coaster.jpg";
    private static final String TEST_IMAGE_CONTENT = "test image content";
    private static final String NEW_IMAGE_CONTENT = "new image content";
    private static final int MIN_HEIGHT_140 = 140;
    private static final int MIN_HEIGHT_120 = 120;
    private static final int MIN_HEIGHT_100 = 100;
    private static final int MIN_HEIGHT_150 = 150;
    private static final int MIN_AGE_12 = 12;
    private static final int MIN_AGE_8 = 8;
    private static final int MIN_AGE_6 = 6;
    private static final int MIN_AGE_14 = 14;
    private static final int MIN_WEIGHT_30 = 30;
    private static final int MIN_WEIGHT_25 = 25;
    private static final int MIN_WEIGHT_20 = 20;
    private static final int MIN_WEIGHT_40 = 40;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttractionRepository attractionRepository;

    private Attraction sample;

    @BeforeEach
    void setUp() {
        attractionRepository.deleteAll();
        sample = Attraction.builder()
                .name(ROLLER_COASTER_NAME)
                .intensity(Intensity.HIGH)
                .minimumHeight(MIN_HEIGHT_140)
                .minimumAge(MIN_AGE_12)
                .minimumWeight(MIN_WEIGHT_30)
                .description(EXTREME_COASTER_DESC)
                .photoUrl(PHOTO_URL)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        attractionRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción")
    @Description("Verifica que crear una atracción retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción retorna 201")
    void testCreateAttractionReturnsCreated() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción")
    @Description("Verifica que crear una atracción retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción retorna header Location")
    void testCreateAttractionReturnsLocationHeader() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción")
    @Description("Verifica que crear una atracción retorna ID generado")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción retorna ID")
    void testCreateAttractionReturnsAttractionWithId() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción")
    @Description("Verifica que crear una atracción retorna nombre correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción retorna nombre correcto")
    void testCreateAttractionReturnsCorrectName() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.name").value(ROLLER_COASTER_NAME));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener todas las atracciones retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracciones retorna 200 OK")
    void testGetAllAttractionsPublicReturnsOk() throws Exception {
        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones retorna array vacío inicialmente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones retorna array vacío")
    void testGetAllAttractionsPublicReturnsEmptyArray() throws Exception {
        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones con datos retorna elementos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones con datos retorna elementos")
    void testGetAllAttractionsWithDataReturnsAttractions() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones contiene nombre")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones contiene nombre")
    void testGetAllAttractionsWithDataContainsName() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS))
                .andExpect(jsonPath("$[0].name").value(ROLLER_COASTER_NAME));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que filtrar por altura mínima retorna resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar por altura mínima retorna resultados")
    void testGetAllAttractionsFilterByMinHeightReturnsFiltered() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS).param("minHeight", "150"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que filtrar excluye atracciones con altura alta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar excluye atracciones con altura alta")
    void testGetAllAttractionsFilterByMinHeightExcludesTall() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS).param("minHeight", "100"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracción por ID retorna 200 OK")
    void testGetAttractionByIdExistsReturnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS + "/" + saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción por ID retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracción por ID retorna datos correctos")
    void testGetAttractionByIdExistsReturnsCorrectData() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(get(API_ATTRACTIONS + "/" + saved.getId()))
                .andExpect(jsonPath("$.name").value(ROLLER_COASTER_NAME));
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracción inexistente retorna 404")
    void testGetAttractionByIdNotExistsReturns404() throws Exception {
        mockMvc.perform(get(API_ATTRACTIONS + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción retorna 200 OK")
    void testUpdateAttractionReturnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        sample.setName(SUPER_COASTER_NAME);
        mockMvc.perform(put(API_ATTRACTIONS + "/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción actualiza el nombre")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción actualiza nombre")
    void testUpdateAttractionUpdatesName() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        sample.setName(SUPER_COASTER_NAME);
        mockMvc.perform(put(API_ATTRACTIONS + "/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.name").value(SUPER_COASTER_NAME));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción inexistente retorna 404")
    void testUpdateAttractionNotExistsReturns404() throws Exception {
        mockMvc.perform(put(API_ATTRACTIONS + "/999999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Atracción")
    @Description("Verifica que eliminar atracción retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar atracción retorna 204")
    void testDeleteAttractionExistsReturns204() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(delete(API_ATTRACTIONS + "/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Atracción")
    @Description("Verifica que eliminar atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar atracción inexistente retorna 404")
    void testDeleteAttractionNotExistsReturns404() throws Exception {
        mockMvc.perform(delete(API_ATTRACTIONS + "/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear atracción sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción sin autenticación retorna 401")
    void testCreateAttractionUnauthorizedReturns401() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Seguridad API")
    @Description("Verifica que crear atracción con rol USER retorna 403")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción con rol USER retorna 403")
    void testCreateAttractionUserRoleReturns403() throws Exception {
        mockMvc.perform(post(API_ATTRACTIONS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción Multipart")
    @Description("Verifica que crear atracción multipart retorna 201")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción multipart retorna 201")
    void testCreateAttractionMultipartReturnsCreated() throws Exception {
        AttractionRequest request = new AttractionRequest();
        request.setName(WATER_SLIDE_NAME);
        request.setIntensity(Intensity.MEDIUM);
        request.setMinimumHeight(MIN_HEIGHT_120);
        request.setMinimumAge(MIN_AGE_8);
        request.setMinimumWeight(MIN_WEIGHT_25);
        request.setDescription(FUN_WATER_SLIDE_DESC);
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", TEST_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_ATTRACTIONS)
                        .file(photo)
                        .file(data)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Atracción Multipart")
    @Description("Verifica que crear atracción multipart retorna ID")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear atracción multipart retorna ID")
    void testCreateAttractionMultipartReturnsId() throws Exception {
        AttractionRequest request = new AttractionRequest();
        request.setName(WATER_SLIDE_NAME);
        request.setIntensity(Intensity.MEDIUM);
        request.setMinimumHeight(MIN_HEIGHT_120);
        request.setMinimumAge(MIN_AGE_8);
        request.setMinimumWeight(MIN_WEIGHT_25);
        request.setDescription(FUN_WATER_SLIDE_DESC);
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", TEST_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_ATTRACTIONS)
                        .file(photo)
                        .file(data)
                        .with(csrf()))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción Multipart")
    @Description("Verifica que actualizar atracción multipart retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción multipart retorna 200 OK")
    void testUpdateAttractionMultipartReturnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);

        AttractionRequest request = new AttractionRequest();
        request.setName(UPDATED_COASTER_NAME);
        request.setIntensity(Intensity.LOW);
        request.setMinimumHeight(MIN_HEIGHT_100);
        request.setMinimumAge(MIN_AGE_6);
        request.setMinimumWeight(MIN_WEIGHT_20);
        request.setDescription(UPDATED_DESC);
        request.setIsActive(false);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_ATTRACTIONS + "/" + saved.getId())
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción Multipart")
    @Description("Verifica que actualizar atracción multipart con foto actualiza datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción multipart con foto actualiza datos")
    void testUpdateAttractionMultipartWithPhotoUpdatesData() throws Exception {
        Attraction saved = attractionRepository.save(sample);

        AttractionRequest request = new AttractionRequest();
        request.setName(PHOTO_UPDATED_COASTER_NAME);
        request.setIntensity(Intensity.HIGH);
        request.setMinimumHeight(MIN_HEIGHT_150);
        request.setMinimumAge(MIN_AGE_14);
        request.setMinimumWeight(MIN_WEIGHT_40);
        request.setDescription(PHOTO_UPDATED_DESC);
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "new.jpg", "image/jpeg", NEW_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_ATTRACTIONS + "/" + saved.getId())
                        .file(photo)
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(jsonPath("$.name").value(PHOTO_UPDATED_COASTER_NAME));
    }
}

