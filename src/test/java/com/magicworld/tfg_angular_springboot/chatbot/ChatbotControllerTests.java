package com.magicworld.tfg_angular_springboot.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtAuthenticationFilter;
import com.magicworld.tfg_angular_springboot.configuration.jwt.JwtService;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationFailureHandler;
import com.magicworld.tfg_angular_springboot.configuration.oauth2.OAuth2AuthenticationSuccessHandler;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatbotController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Epic("Chatbot IA")
@Feature("API REST de Chatbot")
public class ChatbotControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GeminiService geminiService;

    @Test
    @Story("Enviar Mensaje")
    @Description("Verifica que enviar mensaje retorna 200 OK")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Enviar mensaje retorna 200 OK")
    public void testSendMessageReturnsOk() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();

        ChatResponse response = ChatResponse.builder()
                .message("Hello! How can I help you?")
                .success(true)
                .build();

        when(geminiService.processMessage(any(ChatRequest.class))).thenReturn(response);

        var result = mockMvc.perform(post("/api/v1/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Story("Enviar Mensaje")
    @Description("Verifica que enviar mensaje retorna mensaje de respuesta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Enviar mensaje retorna mensaje")
    public void testSendMessageReturnsMessage() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();

        ChatResponse response = ChatResponse.builder()
                .message("Hello! How can I help you?")
                .success(true)
                .build();

        when(geminiService.processMessage(any(ChatRequest.class))).thenReturn(response);

        var result = mockMvc.perform(post("/api/v1/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.message").value("Hello! How can I help you?"))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("Hello! How can I help you?"));
    }

    @Test
    @Story("Enviar Mensaje")
    @Description("Verifica que enviar mensaje retorna success true")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Enviar mensaje retorna success")
    public void testSendMessageReturnsSuccess() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();

        ChatResponse response = ChatResponse.builder()
                .message("Hello! How can I help you?")
                .success(true)
                .build();

        when(geminiService.processMessage(any(ChatRequest.class))).thenReturn(response);

        var result = mockMvc.perform(post("/api/v1/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("true"));
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que mensaje vacío retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mensaje vacío retorna 400")
    public void testSendMessageEmptyMessageReturnsBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("")
                .build();

        var result = mockMvc.perform(post("/api/v1/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    @Story("Validación de Entrada")
    @Description("Verifica que mensaje null retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mensaje null retorna 400")
    public void testSendMessageNullMessageReturnsBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message(null)
                .build();

        var result = mockMvc.perform(post("/api/v1/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @TestConfiguration
    static class ChatbotControllerTestConfig {
        @Bean
        public GeminiService geminiService() {
            return Mockito.mock(GeminiService.class);
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
