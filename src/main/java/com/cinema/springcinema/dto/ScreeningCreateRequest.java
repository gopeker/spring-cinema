package com.cinema.springcinema.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record ScreeningCreateRequest(
        @NotNull Long movieId,
        @NotNull Long showroomId,
        @NotNull LocalDateTime startTime,
        @NotNull BigDecimal basePrice) {
}
