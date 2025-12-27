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
                .name("Roller Coaster")
                .intensity(Intensity.HIGH)
                .minimumHeight(140)
                .minimumAge(12)
                .minimumWeight(30)
                .description("Extreme roller coaster")
                .photoUrl("https://example.com/coaster.jpg")
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
    void testCreateAttraction_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
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
    void testCreateAttraction_returnsLocationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
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
    void testCreateAttraction_returnsAttractionWithId() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
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
    void testCreateAttraction_returnsCorrectName() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.name").value("Roller Coaster"));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener todas las atracciones retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracciones retorna 200 OK")
    void testGetAllAttractions_public_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/attractions"))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones retorna array vacío inicialmente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones retorna array vacío")
    void testGetAllAttractions_public_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/attractions"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones con datos retorna elementos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones con datos retorna elementos")
    void testGetAllAttractions_withData_returnsAttractions() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Listar Atracciones")
    @Description("Verifica que obtener atracciones contiene nombre")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracciones contiene nombre")
    void testGetAllAttractions_withData_containsName() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions"))
                .andExpect(jsonPath("$[0].name").value("Roller Coaster"));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que filtrar por altura mínima retorna resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar por altura mínima retorna resultados")
    void testGetAllAttractions_filterByMinHeight_returnsFiltered() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions").param("minHeight", "150"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Filtrar Atracciones")
    @Description("Verifica que filtrar excluye atracciones con altura alta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Filtrar excluye atracciones con altura alta")
    void testGetAllAttractions_filterByMinHeight_excludesTall() throws Exception {
        attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions").param("minHeight", "100"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener atracción por ID retorna 200 OK")
    void testGetAttractionById_exists_returnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions/" + saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción por ID retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracción por ID retorna datos correctos")
    void testGetAttractionById_exists_returnsCorrectData() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(get("/api/v1/attractions/" + saved.getId()))
                .andExpect(jsonPath("$.name").value("Roller Coaster"));
    }

    @Test
    @Story("Obtener Atracción por ID")
    @Description("Verifica que obtener atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener atracción inexistente retorna 404")
    void testGetAttractionById_notExists_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/attractions/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar atracción retorna 200 OK")
    void testUpdateAttraction_returnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        sample.setName("Super Coaster");
        mockMvc.perform(put("/api/v1/attractions/" + saved.getId())
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
    void testUpdateAttraction_updatesName() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        sample.setName("Super Coaster");
        mockMvc.perform(put("/api/v1/attractions/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.name").value("Super Coaster"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Atracción")
    @Description("Verifica que actualizar atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar atracción inexistente retorna 404")
    void testUpdateAttraction_notExists_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/attractions/999999")
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
    void testDeleteAttraction_exists_returns204() throws Exception {
        Attraction saved = attractionRepository.save(sample);
        mockMvc.perform(delete("/api/v1/attractions/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Atracción")
    @Description("Verifica que eliminar atracción inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar atracción inexistente retorna 404")
    void testDeleteAttraction_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/attractions/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear atracción sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear atracción sin autenticación retorna 401")
    void testCreateAttraction_unauthorized_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
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
    void testCreateAttraction_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/attractions")
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
    void testCreateAttractionMultipart_returnsCreated() throws Exception {
        AttractionRequest request = new AttractionRequest();
        request.setName("Water Slide");
        request.setIntensity(Intensity.MEDIUM);
        request.setMinimumHeight(120);
        request.setMinimumAge(8);
        request.setMinimumWeight(25);
        request.setDescription("Fun water slide");
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/attractions")
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
    void testCreateAttractionMultipart_returnsId() throws Exception {
        AttractionRequest request = new AttractionRequest();
        request.setName("Water Slide");
        request.setIntensity(Intensity.MEDIUM);
        request.setMinimumHeight(120);
        request.setMinimumAge(8);
        request.setMinimumWeight(25);
        request.setDescription("Fun water slide");
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/attractions")
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
    void testUpdateAttractionMultipart_returnsOk() throws Exception {
        Attraction saved = attractionRepository.save(sample);

        AttractionRequest request = new AttractionRequest();
        request.setName("Updated Coaster");
        request.setIntensity(Intensity.LOW);
        request.setMinimumHeight(100);
        request.setMinimumAge(6);
        request.setMinimumWeight(20);
        request.setDescription("Updated description");
        request.setIsActive(false);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/attractions/" + saved.getId())
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
    void testUpdateAttractionMultipart_withPhoto_updatesData() throws Exception {
        Attraction saved = attractionRepository.save(sample);

        AttractionRequest request = new AttractionRequest();
        request.setName("Photo Updated Coaster");
        request.setIntensity(Intensity.HIGH);
        request.setMinimumHeight(150);
        request.setMinimumAge(14);
        request.setMinimumWeight(40);
        request.setDescription("Photo updated description");
        request.setIsActive(true);

        MockMultipartFile photo = new MockMultipartFile("photo", "new.jpg", "image/jpeg", "new image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/attractions/" + saved.getId())
                        .file(photo)
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(jsonPath("$.name").value("Photo Updated Coaster"));
    }
}

