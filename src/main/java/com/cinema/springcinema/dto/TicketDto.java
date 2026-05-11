package com.cinema.springcinema.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketDto(
        Long id,
        ScreeningDto screening,
        String seatRow,
        Integer seatNumber,
        BigDecimal price,
        String status,
        LocalDateTime purchaseTime) {
}
