package com.cinema.springcinema.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieDto(
        Long id,
        String title,
        String description,
        Integer duration,
        @JsonProperty("posterUrl") String posterUrl) {
}
