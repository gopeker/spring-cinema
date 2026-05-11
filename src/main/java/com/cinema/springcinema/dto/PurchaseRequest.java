package com.cinema.springcinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(
        @NotNull Long screeningId,
        @NotBlank String seatRow,
        @NotNull Integer seatNumber) {
}
