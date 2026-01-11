package com.magicworld.tfg_angular_springboot.discount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.discount_ticket_type.DiscountTicketTypeRepository;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketType;
import com.magicworld.tfg_angular_springboot.ticket_type.TicketTypeRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Gestión de Descuentos")
@Feature("Tests E2E de Descuentos")
public class DiscountE2ETests {

    private static final String API_DISCOUNTS = "/api/v1/discounts";
    private static final String DISCOUNT_CODE_SAVE20 = "SAVE20";
    private static final String TYPE_NAME_ADULT = "ADULT";
    private static final String CURRENCY_EUR = "EUR";
    private static final String ADULT_TICKET_DESC = "Adult ticket";
    private static final String PHOTO_URL_ADULT = "https://example.com/adult.jpg";
    private static final BigDecimal COST_50 = new BigDecimal("50.00");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private DiscountTicketTypeRepository discountTicketTypeRepository;

    private DiscountController.DiscountRequest discountRequest;

    @BeforeEach
    void setUp() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();

        TicketType ticketType = ticketTypeRepository.save(TicketType.builder()
                .typeName(TYPE_NAME_ADULT)
                .cost(COST_50)
                .description(ADULT_TICKET_DESC)
                .maxPerDay(100)
                .photoUrl(PHOTO_URL_ADULT)
                .build());

        discountRequest = new DiscountController.DiscountRequest();
        discountRequest.setDiscount(Discount.builder()
                .discountCode(DISCOUNT_CODE_SAVE20)
                .discountPercentage(20)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());
        discountRequest.setApplicableTicketTypesNames(List.of(TYPE_NAME_ADULT));
    }

    @AfterEach
    void tearDown() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna estado 201 Created")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear descuento retorna 201 Created")
    void testCreateDiscountReturnsCreated() throws Exception {
        var result = mockMvc.perform(post(API_DISCOUNTS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna header Location")
    void testCreateDiscountReturnsLocationHeader() throws Exception {
        var result = mockMvc.perform(post(API_DISCOUNTS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(header().exists("Location"))
                .andReturn();
        assertNotNull(result.getResponse().getHeader("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna descuento con ID")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna descuento con ID")
    void testCreateDiscountReturnsDiscountWithId() throws Exception {
        var result = mockMvc.perform(post(API_DISCOUNTS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("id"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna código correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna código correcto")
    void testCreateDiscountReturnsCorrectCode() throws Exception {
        var result = mockMvc.perform(post(API_DISCOUNTS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(jsonPath("$.discountCode").value(DISCOUNT_CODE_SAVE20))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(DISCOUNT_CODE_SAVE20));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que usuario autenticado puede obtener descuentos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Usuario autenticado obtiene descuentos con 200 OK")
    void testGetAllDiscountsAuthenticatedReturnsOk() throws Exception {
        var result = mockMvc.perform(get(API_DISCOUNTS))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que lista vacía retorna array vacío")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Lista vacía retorna array vacío")
    void testGetAllDiscountsEmptyReturnsEmptyArray() throws Exception {
        var result = mockMvc.perform(get(API_DISCOUNTS))
                .andExpect(jsonPath("$.length()").value(0))
                .andReturn();
        assertEquals("[]", result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que con datos retorna descuentos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con datos retorna descuentos")
    void testGetAllDiscountsWithDataReturnsDiscounts() throws Exception {
        discountRepository.save(discountRequest.getDiscount());
        var result = mockMvc.perform(get(API_DISCOUNTS))
                .andExpect(jsonPath("$.length()").value(1))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(DISCOUNT_CODE_SAVE20));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que con datos contiene código del descuento")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con datos contiene código del descuento")
    void testGetAllDiscountsWithDataContainsCode() throws Exception {
        discountRepository.save(discountRequest.getDiscount());
        var result = mockMvc.perform(get(API_DISCOUNTS))
                .andExpect(jsonPath("$[0].discountCode").value(DISCOUNT_CODE_SAVE20))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(DISCOUNT_CODE_SAVE20));
    }


    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar descuento existente retorna 200 OK")
    void testGetDiscountByIdExistsReturnsOk() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        var result = mockMvc.perform(get(API_DISCOUNTS + "/" + saved.getId()))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento existente retorna datos correctas")
    void testGetDiscountByIdExistsReturnsCorrectData() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        var result = mockMvc.perform(get(API_DISCOUNTS + "/" + saved.getId()))
                .andExpect(jsonPath("$.discountCode").value(DISCOUNT_CODE_SAVE20))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(DISCOUNT_CODE_SAVE20));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento inexistente retorna 404")
    void testGetDiscountByIdNotExistsReturns404() throws Exception {
        var result = mockMvc.perform(get(API_DISCOUNTS + "/999999"))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento existente retorna 204")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar descuento existente retorna 204")
    void testDeleteDiscountExistsReturns204() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        var result = mockMvc.perform(delete(API_DISCOUNTS + "/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();
        assertEquals(204, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar descuento inexistente retorna 404")
    void testDeleteDiscountNotExistsReturns404() throws Exception {
        var result = mockMvc.perform(delete(API_DISCOUNTS + "/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    @Story("Seguridad")
    @Description("Verifica que obtener descuentos sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin autenticación obtener descuentos retorna 401")
    void testGetAllDiscountsUnauthorizedReturns401() throws Exception {
        var result = mockMvc.perform(get(API_DISCOUNTS))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @Story("Seguridad")
    @Description("Verifica que crear descuento sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin autenticación crear descuento retorna 401")
    void testCreateDiscountUnauthorizedReturns401() throws Exception {
        var result = mockMvc.perform(post(API_DISCOUNTS)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();
        assertEquals(401, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Obtener Tipos de Entrada por Descuento")
    @Description("Verifica que obtener tipos de entrada por descuento retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipos de entrada por descuento retorna 200 OK")
    void testGetTicketTypesByDiscountReturnsOk() throws Exception {
        Discount discount = discountRequest.getDiscount();
        discount = discountRepository.save(discount);
        var result = mockMvc.perform(get(API_DISCOUNTS + "/" + discount.getId() + "/ticket-types"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
}
