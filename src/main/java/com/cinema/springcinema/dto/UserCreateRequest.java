package com.cinema.springcinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password,
        String role,
        AddressDto address,
        PaymentDetailsDto paymentDetails) {}
