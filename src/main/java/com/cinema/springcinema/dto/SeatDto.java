package com.cinema.springcinema.dto;

import java.math.BigDecimal;

public record SeatDto(String seatRow, Integer seatNumber, boolean available, BigDecimal price, String tier) {
}
