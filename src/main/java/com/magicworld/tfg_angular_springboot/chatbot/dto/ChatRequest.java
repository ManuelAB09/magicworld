package com.magicworld.tfg_angular_springboot.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    private List<ChatHistoryEntry> history;

    private PendingAction pendingAction;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryEntry {
        private String role; // "user" o "assistant"
        private String content;
    }
}

