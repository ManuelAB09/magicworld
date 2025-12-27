package com.magicworld.tfg_angular_springboot.ticket_type;

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

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Tipos de Entrada")
@Feature("API REST de Tipos de Entrada E2E")
public class TicketTypeE2ETests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    private TicketType sample;

    @BeforeEach
    void setUp() {
        ticketTypeRepository.deleteAll();
        sample = TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build();
    }

    @AfterEach
    void tearDown() {
        ticketTypeRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear un tipo de entrada retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna 201")
    void testCreateTicketType_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear un tipo de entrada retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo de entrada retorna header Location")
    void testCreateTicketType_returnsLocationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear un tipo de entrada retorna el ID generado")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo de entrada retorna ID")
    void testCreateTicketType_returnsTicketTypeWithId() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada")
    @Description("Verifica que crear un tipo de entrada retorna el nombre correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo de entrada retorna nombre correcto")
    void testCreateTicketType_returnsCorrectTypeName() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.typeName").value("ADULT"));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los tipos retorna 200 OK")
    void testGetAllTicketTypes_public_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/ticket-types"))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna array vacío inicialmente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos retorna array vacío")
    void testGetAllTicketTypes_public_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/ticket-types"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna los datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos retorna datos")
    void testGetAllTicketTypes_withData_returnsTicketTypes() throws Exception {
        ticketTypeRepository.save(sample);
        mockMvc.perform(get("/api/v1/ticket-types"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos contiene nombre")
    void testGetAllTicketTypes_withData_containsTypeName() throws Exception {
        ticketTypeRepository.save(sample);
        mockMvc.perform(get("/api/v1/ticket-types"))
                .andExpect(jsonPath("$[0].typeName").value("ADULT"));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener tipo por ID retorna 200 OK")
    void testGetTicketTypeById_exists_returnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(get("/api/v1/ticket-types/" + saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo por ID retorna datos correctos")
    void testGetTicketTypeById_exists_returnsCorrectData() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(get("/api/v1/ticket-types/" + saved.getId()))
                .andExpect(jsonPath("$.typeName").value("ADULT"));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo inexistente retorna 404")
    void testGetTicketTypeById_notExists_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/ticket-types/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo retorna 200 OK")
    void testUpdateTicketType_returnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        sample.setTypeName("PREMIUM");
        mockMvc.perform(put("/api/v1/ticket-types/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo actualiza el nombre")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo actualiza el nombre")
    void testUpdateTicketType_updatesTypeName() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        sample.setTypeName("PREMIUM");
        mockMvc.perform(put("/api/v1/ticket-types/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.typeName").value("PREMIUM"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo inexistente retorna 404")
    void testUpdateTicketType_notExists_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/ticket-types/999999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que eliminar tipo retorna 204 No Content")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar tipo retorna 204 No Content")
    void testDeleteTicketType_exists_returns204() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(delete("/api/v1/ticket-types/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que eliminar tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar tipo inexistente retorna 404")
    void testDeleteTicketType_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/ticket-types/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear tipo sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo sin autenticación retorna 401")
    void testCreateTicketType_unauthorized_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Seguridad API")
    @Description("Verifica que crear tipo con rol USER retorna 403")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo con rol USER retorna 403")
    void testCreateTicketType_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/ticket-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada Multipart")
    @Description("Verifica que crear tipo multipart retorna 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo multipart retorna 201")
    void testCreateTicketTypeMultipart_returnsCreated() throws Exception {
        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(new BigDecimal("75.00"));
        request.setCurrency("USD");
        request.setTypeName("VIP");
        request.setDescription("VIP ticket");
        request.setMaxPerDay(50);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/ticket-types")
                        .file(photo)
                        .file(data)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Tipo de Entrada Multipart")
    @Description("Verifica que crear tipo multipart retorna ID")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear tipo multipart retorna ID")
    void testCreateTicketTypeMultipart_returnsId() throws Exception {
        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(new BigDecimal("75.00"));
        request.setCurrency("USD");
        request.setTypeName("VIP");
        request.setDescription("VIP ticket");
        request.setMaxPerDay(50);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", "test image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/ticket-types")
                        .file(photo)
                        .file(data)
                        .with(csrf()))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada Multipart")
    @Description("Verifica que actualizar tipo multipart retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo multipart retorna 200 OK")
    void testUpdateTicketTypeMultipart_returnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);

        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(new BigDecimal("100.00"));
        request.setCurrency("EUR");
        request.setTypeName("PREMIUM");
        request.setDescription("Premium ticket");
        request.setMaxPerDay(200);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/ticket-types/" + saved.getId())
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada Multipart")
    @Description("Verifica que actualizar tipo multipart con foto actualiza datos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo multipart con foto actualiza datos")
    void testUpdateTicketTypeMultipart_withPhoto_updatesData() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);

        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(new BigDecimal("150.00"));
        request.setCurrency("USD");
        request.setTypeName("ULTRA");
        request.setDescription("Ultra ticket");
        request.setMaxPerDay(25);

        MockMultipartFile photo = new MockMultipartFile("photo", "new.jpg", "image/jpeg", "new image content".getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/v1/ticket-types/" + saved.getId())
                        .file(photo)
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(jsonPath("$.typeName").value("ULTRA"));
    }
}

