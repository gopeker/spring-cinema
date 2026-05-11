package com.cinema.springcinema.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.cinema.springcinema.dto.MovieDto;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private final ChatClient.Builder chatClientBuilder;
    private final MovieService movieService;
    private final List<ChatMessage> chatHistory = new ArrayList<>();

    public ChatbotService(ChatClient.Builder chatClientBuilder, MovieService movieService) {
        this.chatClientBuilder = chatClientBuilder;
        this.movieService = movieService;
    }

    private record ChatMessage(String role, String content) {}

    private String buildSystemPrompt() {
        List<MovieDto> movies = movieService.findAll();

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a friendly movie recommendation assistant for Spring Cinema. ");
        prompt.append("Your role is to help users find movies they might enjoy.\n\n");

        prompt.append("Available movies:\n");
        for (MovieDto movie : movies) {
            prompt.append("- ").append(movie.title());
            if (movie.description() != null && !movie.description().isEmpty()) {
                prompt.append(": ").append(movie.description());
            }
            if (movie.duration() != null) {
                prompt.append(" (").append(movie.duration()).append(" min)");
            }
            prompt.append("\n");
        }

        prompt.append("\nGuidelines:\n");
        prompt.append("- Recommend only movies from our list\n");
        prompt.append("- Ask about preferences\n");
        prompt.append("- Be friendly and brief\n");
        prompt.append("- Remember user preferences\n");
        prompt.append("- Direct showtime questions to the movie page\n");

        return prompt.toString();
    }

    public String chat(String userMessage) {
        chatHistory.add(new ChatMessage("user", userMessage));

        try {
            StringBuilder conversation = new StringBuilder();
            conversation.append(buildSystemPrompt()).append("\n\n");

            for (ChatMessage msg : chatHistory) {
                String roleLabel = msg.role().equals("user") ? "User" : "Assistant";
                conversation.append(roleLabel).append(": ").append(msg.content()).append("\n");
            }
            conversation.append("Assistant:");

            ChatClient chatClient = chatClientBuilder.build();

            String response = chatClient
                    .prompt()
                    .user(conversation.toString())
                    .call()
                    .content();

            if (response == null || response.isEmpty()) {
                log.warn("Empty response from LM Studio");
                return "I didn't get a response. Please try again.";
            }

            chatHistory.add(new ChatMessage("assistant", response));

            return response;
        } catch (Exception e) {
            log.error("Error calling LM Studio: {}", e.getMessage(), e);
            return "Sorry, I'm having trouble connecting to the AI. Make sure LM Studio is running with a model loaded at http://127.0.0.1:1234";
        }
    }
}
