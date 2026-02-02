package com.magicworld.tfg_angular_springboot.purchase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import com.magicworld.tfg_angular_springboot.purchase_line.PurchaseLineRepository;
import com.magicworld.tfg_angular_springboot.user.Role;
import com.magicworld.tfg_angular_springboot.user.User;
import com.magicworld.tfg_angular_springboot.user.UserRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PurchaseController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Gestión de Compras")
@Feature("Controlador de Compras")
public class PurchaseControllerTests {

    private static final String API_MY_PURCHASES = "/api/v1/purchases/my-purchases";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseLineRepository purchaseLineRepository;

    @Autowired
    private UserRepository userRepository;

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("test@example.com");
        user.setUserRole(Role.USER);
        return user;
    }

    private Purchase samplePurchase(User buyer) {
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setPurchaseDate(LocalDate.now());
        purchase.setBuyer(buyer);
        return purchase;
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Listar Mis Compras")
    @Description("Verifica que listar mis compras retorna 200 OK")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar mis compras retorna 200 OK")
    void testGetMyPurchasesReturnsOk() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(purchaseService.findByBuyerId(1L)).thenReturn(List.of());

        mockMvc.perform(get(API_MY_PURCHASES))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Listar Mis Compras")
    @Description("Verifica que listar mis compras sin compras retorna lista vacía")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Listar mis compras sin compras retorna lista vacía")
    void testGetMyPurchasesEmptyReturnsEmptyList() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(purchaseService.findByBuyerId(1L)).thenReturn(List.of());

        mockMvc.perform(get(API_MY_PURCHASES))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Listar Mis Compras")
    @Description("Verifica que listar mis compras retorna datos de compra")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar mis compras retorna datos de compra")
    void testGetMyPurchasesReturnsPurchaseData() throws Exception {
        User user = sampleUser();
        Purchase purchase = samplePurchase(user);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(purchaseService.findByBuyerId(1L)).thenReturn(List.of(purchase));
        when(purchaseLineRepository.findByPurchaseId(1L)).thenReturn(List.of());

        mockMvc.perform(get(API_MY_PURCHASES))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    @Story("Listar Mis Compras")
    @Description("Verifica que se llama al servicio con el userId correcto")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Listar mis compras llama al servicio con userId correcto")
    void testGetMyPurchasesCallsServiceWithCorrectUserId() throws Exception {
        User user = sampleUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(purchaseService.findByBuyerId(1L)).thenReturn(List.of());

        mockMvc.perform(get(API_MY_PURCHASES));

        verify(purchaseService, atLeastOnce()).findByBuyerId(1L);
    }

    @TestConfiguration
    static class PurchaseControllerTestConfig {
        @Bean
        public PurchaseService purchaseService() {
            return Mockito.mock(PurchaseService.class);
        }

        @Bean
        public PurchaseLineRepository purchaseLineRepository() {
            return Mockito.mock(PurchaseLineRepository.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }

        @Bean
        public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
            return Mockito.mock(OAuth2AuthenticationSuccessHandler.class);
        }

        @Bean
        public OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
            return Mockito.mock(OAuth2AuthenticationFailureHandler.class);
        }
    }
}
