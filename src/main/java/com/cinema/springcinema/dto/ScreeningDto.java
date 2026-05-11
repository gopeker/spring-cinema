package com.cinema.springcinema.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScreeningDto(
        Long id,
        MovieDto movie,
        ShowroomDto showroom,
        LocalDateTime startTime,
        BigDecimal basePrice) {
}
