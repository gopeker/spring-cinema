package com.cinema.springcinema.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.ChatRequest;
import com.cinema.springcinema.dto.ChatResponse;
import com.cinema.springcinema.service.ChatbotService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String response = chatbotService.chat(request.message(), request.safeHistory());
        return ResponseEntity.ok(new ChatResponse(response));
    }
}
