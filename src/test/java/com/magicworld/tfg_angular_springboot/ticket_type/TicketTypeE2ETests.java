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

    private static final String API_TICKET_TYPES = "/api/v1/ticket-types";
    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String TYPE_NAME_PREMIUM = "PREMIUM";
    private static final String TYPE_NAME_VIP = "VIP";
    private static final String TYPE_NAME_ULTRA = "ULTRA";
    private static final String ADULT_TICKET_DESC = "Adult ticket";
    private static final String VIP_TICKET_DESC = "VIP ticket";
    private static final String PREMIUM_TICKET_DESC = "Premium ticket";
    private static final String ULTRA_TICKET_DESC = "Ultra ticket";
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String PHOTO_URL_ADULT = "https://example.com/adult.jpg";
    private static final String TEST_IMAGE_CONTENT = "test image content";
    private static final String NEW_IMAGE_CONTENT = "new image content";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");
    private static final BigDecimal COST_75 = new BigDecimal("75.00");
    private static final BigDecimal COST_100 = new BigDecimal("100.00");
    private static final BigDecimal COST_150 = new BigDecimal("150.00");

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
                .typeName(TYPE_NAME_ADULT)
                .cost(COST_50)
                .currency(CURRENCY_EUR)
                .description(ADULT_TICKET_DESC)
                .maxPerDay(100)
                .photoUrl(PHOTO_URL_ADULT)
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
    void testCreateTicketTypeReturnsCreated() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
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
    void testCreateTicketTypeReturnsLocationHeader() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
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
    void testCreateTicketTypeReturnsTicketTypeWithId() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
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
    void testCreateTicketTypeReturnsCorrectTypeName() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.typeName").value(TYPE_NAME_ADULT));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener todos los tipos retorna 200 OK")
    void testGetAllTicketTypesPublicReturnsOk() throws Exception {
        mockMvc.perform(get(API_TICKET_TYPES))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna array vacío inicialmente")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos retorna array vacío")
    void testGetAllTicketTypesPublicReturnsEmptyArray() throws Exception {
        mockMvc.perform(get(API_TICKET_TYPES))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos retorna los datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos retorna datos")
    void testGetAllTicketTypesWithDataReturnsTicketTypes() throws Exception {
        ticketTypeRepository.save(sample);
        mockMvc.perform(get(API_TICKET_TYPES))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Story("Listar Tipos de Entrada")
    @Description("Verifica que obtener todos los tipos contiene el nombre del tipo")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener todos los tipos contiene nombre")
    void testGetAllTicketTypesWithDataContainsTypeName() throws Exception {
        ticketTypeRepository.save(sample);
        mockMvc.perform(get(API_TICKET_TYPES))
                .andExpect(jsonPath("$[0].typeName").value(TYPE_NAME_ADULT));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Obtener tipo por ID retorna 200 OK")
    void testGetTicketTypeByIdExistsReturnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(get(API_TICKET_TYPES + "/" + saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo por ID retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo por ID retorna datos correctos")
    void testGetTicketTypeByIdExistsReturnsCorrectData() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(get(API_TICKET_TYPES + "/" + saved.getId()))
                .andExpect(jsonPath("$.typeName").value(TYPE_NAME_ADULT));
    }

    @Test
    @Story("Obtener Tipo de Entrada por ID")
    @Description("Verifica que obtener tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipo inexistente retorna 404")
    void testGetTicketTypeByIdNotExistsReturns404() throws Exception {
        mockMvc.perform(get(API_TICKET_TYPES + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar tipo retorna 200 OK")
    void testUpdateTicketTypeReturnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        sample.setTypeName(TYPE_NAME_PREMIUM);
        mockMvc.perform(put(API_TICKET_TYPES + "/" + saved.getId())
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
    void testUpdateTicketTypeUpdatesTypeName() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        sample.setTypeName(TYPE_NAME_PREMIUM);
        mockMvc.perform(put(API_TICKET_TYPES + "/" + saved.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sample)))
                .andExpect(jsonPath("$.typeName").value(TYPE_NAME_PREMIUM));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Actualizar Tipo de Entrada")
    @Description("Verifica que actualizar tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Actualizar tipo inexistente retorna 404")
    void testUpdateTicketTypeNotExistsReturns404() throws Exception {
        mockMvc.perform(put(API_TICKET_TYPES + "/999999")
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
    void testDeleteTicketTypeExistsReturns204() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);
        mockMvc.perform(delete(API_TICKET_TYPES + "/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Tipo de Entrada")
    @Description("Verifica que eliminar tipo inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar tipo inexistente retorna 404")
    void testDeleteTicketTypeNotExistsReturns404() throws Exception {
        mockMvc.perform(delete(API_TICKET_TYPES + "/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Seguridad API")
    @Description("Verifica que crear tipo sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear tipo sin autenticación retorna 401")
    void testCreateTicketTypeUnauthorizedReturns401() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
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
    void testCreateTicketTypeUserRoleReturns403() throws Exception {
        mockMvc.perform(post(API_TICKET_TYPES)
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
    void testCreateTicketTypeMultipartReturnsCreated() throws Exception {
        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(COST_75);
        request.setCurrency(CURRENCY_USD);
        request.setTypeName(TYPE_NAME_VIP);
        request.setDescription(VIP_TICKET_DESC);
        request.setMaxPerDay(50);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", TEST_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_TICKET_TYPES)
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
    void testCreateTicketTypeMultipartReturnsId() throws Exception {
        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(COST_75);
        request.setCurrency(CURRENCY_USD);
        request.setTypeName(TYPE_NAME_VIP);
        request.setDescription(VIP_TICKET_DESC);
        request.setMaxPerDay(50);

        MockMultipartFile photo = new MockMultipartFile("photo", "test.jpg", "image/jpeg", TEST_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_TICKET_TYPES)
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
    void testUpdateTicketTypeMultipartReturnsOk() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);

        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(COST_100);
        request.setCurrency(CURRENCY_EUR);
        request.setTypeName(TYPE_NAME_PREMIUM);
        request.setDescription(PREMIUM_TICKET_DESC);
        request.setMaxPerDay(200);

        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_TICKET_TYPES + "/" + saved.getId())
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
    void testUpdateTicketTypeMultipartWithPhotoUpdatesData() throws Exception {
        TicketType saved = ticketTypeRepository.save(sample);

        TicketTypeRequest request = new TicketTypeRequest();
        request.setCost(COST_150);
        request.setCurrency(CURRENCY_USD);
        request.setTypeName(TYPE_NAME_ULTRA);
        request.setDescription(ULTRA_TICKET_DESC);
        request.setMaxPerDay(25);

        MockMultipartFile photo = new MockMultipartFile("photo", "new.jpg", "image/jpeg", NEW_IMAGE_CONTENT.getBytes());
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json", objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart(API_TICKET_TYPES + "/" + saved.getId())
                        .file(photo)
                        .file(data)
                        .with(csrf())
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(jsonPath("$.typeName").value(TYPE_NAME_ULTRA));
    }
}

