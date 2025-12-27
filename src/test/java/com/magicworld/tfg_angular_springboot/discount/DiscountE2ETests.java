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

    private TicketType ticketType;
    private DiscountController.DiscountRequest discountRequest;

    @BeforeEach
    void setUp() {
        discountTicketTypeRepository.deleteAll();
        discountRepository.deleteAll();
        ticketTypeRepository.deleteAll();

        ticketType = ticketTypeRepository.save(TicketType.builder()
                .typeName("ADULT")
                .cost(new BigDecimal("50.00"))
                .currency("EUR")
                .description("Adult ticket")
                .maxPerDay(100)
                .photoUrl("https://example.com/adult.jpg")
                .build());

        discountRequest = new DiscountController.DiscountRequest();
        discountRequest.setDiscount(Discount.builder()
                .discountCode("SAVE20")
                .discountPercentage(20)
                .expiryDate(LocalDate.now().plusDays(30))
                .build());
        discountRequest.setApplicableTicketTypesNames(List.of("ADULT"));
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
    void testCreateDiscount_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna header Location")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna header Location")
    void testCreateDiscount_returnsLocationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna descuento con ID")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna descuento con ID")
    void testCreateDiscount_returnsDiscountWithId() throws Exception {
        mockMvc.perform(post("/api/v1/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Crear Descuento")
    @Description("Verifica que crear descuento retorna código correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear descuento retorna código correcto")
    void testCreateDiscount_returnsCorrectCode() throws Exception {
        mockMvc.perform(post("/api/v1/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(jsonPath("$.discountCode").value("SAVE20"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que usuario autenticado puede obtener descuentos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Usuario autenticado obtiene descuentos con 200 OK")
    void testGetAllDiscounts_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que lista vacía retorna array vacío")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Lista vacía retorna array vacío")
    void testGetAllDiscounts_empty_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que con datos retorna descuentos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con datos retorna descuentos")
    void testGetAllDiscounts_withData_returnsDiscounts() throws Exception {
        discountRepository.save(discountRequest.getDiscount());
        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Listar Descuentos")
    @Description("Verifica que con datos contiene código del descuento")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Con datos contiene código del descuento")
    void testGetAllDiscounts_withData_containsCode() throws Exception {
        discountRepository.save(discountRequest.getDiscount());
        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(jsonPath("$[0].discountCode").value("SAVE20"));
    }


    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Buscar descuento existente retorna 200 OK")
    void testGetDiscountById_exists_returnsOk() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        mockMvc.perform(get("/api/v1/discounts/" + saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento existente retorna datos correctos")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento existente retorna datos correctas")
    void testGetDiscountById_exists_returnsCorrectData() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        mockMvc.perform(get("/api/v1/discounts/" + saved.getId()))
                .andExpect(jsonPath("$.discountCode").value("SAVE20"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Buscar Descuento por ID")
    @Description("Verifica que buscar descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Buscar descuento inexistente retorna 404")
    void testGetDiscountById_notExists_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/discounts/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento existente retorna 204")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Eliminar descuento existente retorna 204")
    void testDeleteDiscount_exists_returns204() throws Exception {
        Discount saved = discountRepository.save(discountRequest.getDiscount());
        mockMvc.perform(delete("/api/v1/discounts/" + saved.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Eliminar Descuento")
    @Description("Verifica que eliminar descuento inexistente retorna 404")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Eliminar descuento inexistente retorna 404")
    void testDeleteDiscount_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/discounts/999999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Story("Seguridad")
    @Description("Verifica que obtener descuentos sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin autenticación obtener descuentos retorna 401")
    void testGetAllDiscounts_unauthorized_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/discounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Story("Seguridad")
    @Description("Verifica que crear descuento sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Sin autenticación crear descuento retorna 401")
    void testCreateDiscount_unauthorized_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/discounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(discountRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Obtener Tipos de Entrada por Descuento")
    @Description("Verifica que obtener tipos de entrada por descuento retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Obtener tipos de entrada por descuento retorna 200 OK")
    void testGetTicketTypesByDiscount_returnsOk() throws Exception {
        Discount discount = discountRequest.getDiscount();
        discount = discountRepository.save(discount);
        mockMvc.perform(get("/api/v1/discounts/" + discount.getId() + "/ticket-types"))
                .andExpect(status().isOk());
    }
}
