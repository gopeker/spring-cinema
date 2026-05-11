package com.cinema.springcinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MovieCreateRequest(
        @NotBlank String title,
        String description,
        @NotNull Integer duration,
        String posterUrl) {
}
