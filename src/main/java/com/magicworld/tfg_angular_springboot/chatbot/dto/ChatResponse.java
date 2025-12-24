package com.magicworld.tfg_angular_springboot.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String message;

    private PendingAction pendingAction;

    private boolean success;

    private Object data;
}

