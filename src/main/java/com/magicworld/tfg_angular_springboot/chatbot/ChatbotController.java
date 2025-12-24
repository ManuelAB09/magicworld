package com.magicworld.tfg_angular_springboot.chatbot;

import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatRequest;
import com.magicworld.tfg_angular_springboot.chatbot.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final GeminiService geminiService;

    @Operation(summary = "Send message to chatbot",
               description = "Send a message to the AI chatbot for admin operations",
               tags = {"Chatbot"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message processed successfully",
                        content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody @Valid ChatRequest request) {
        ChatResponse response = geminiService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}

