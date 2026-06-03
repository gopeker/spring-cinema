package com.cinema.springcinema.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ChatRequest(
        @NotBlank String message,
        List<ChatMessageDto> history
) {
    public record ChatMessageDto(String role, String content) {}

    /** Returns history as a non-null list even when the client omits it. */
    public List<ChatMessageDto> safeHistory() {
        return history != null ? history : List.of();
    }
}
