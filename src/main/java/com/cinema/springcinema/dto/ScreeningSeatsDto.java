package com.cinema.springcinema.dto;

import java.util.List;

public record ScreeningSeatsDto(ScreeningDto screening, List<SeatDto> seats) {
}
