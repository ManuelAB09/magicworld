package com.magicworld.tfg_angular_springboot.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAction {

    private String actionType;

    private Map<String, Object> params;

    private String confirmationMessage;
}

