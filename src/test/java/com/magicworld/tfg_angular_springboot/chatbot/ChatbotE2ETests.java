package com.magicworld.tfg_angular_springboot.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc
@Epic("Chatbot IA")
@Feature("API REST de Chatbot E2E")
public class ChatbotE2ETests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Story("Seguridad API")
    @Description("Verifica que enviar mensaje sin autenticación retorna 401")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Enviar mensaje sin autenticación retorna 401")
    void testSendMessage_unauthorized_returns401() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();
        mockMvc.perform(post("/api/v1/chatbot/message")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @Story("Seguridad API")
    @Description("Verifica que enviar mensaje con rol USER retorna 403")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Enviar mensaje con rol USER retorna 403")
    void testSendMessage_userRole_returns403() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .build();
        mockMvc.perform(post("/api/v1/chatbot/message")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Validación de Entrada")
    @Description("Verifica que mensaje vacío retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mensaje vacío retorna 400")
    void testSendMessage_emptyMessage_returnsBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("")
                .build();
        mockMvc.perform(post("/api/v1/chatbot/message")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Validación de Entrada")
    @Description("Verifica que mensaje null retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mensaje null retorna 400")
    void testSendMessage_nullMessage_returnsBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message(null)
                .build();
        mockMvc.perform(post("/api/v1/chatbot/message")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Story("Validación de Entrada")
    @Description("Verifica que mensaje en blanco retorna 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Mensaje en blanco retorna 400")
    void testSendMessage_blankMessage_returnsBadRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("   ")
                .build();
        mockMvc.perform(post("/api/v1/chatbot/message")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
