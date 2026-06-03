package com.cinema.springcinema.dto;

public record UserSearchRequest(
        String name,
        String email,
        String street,
        String city,
        String postalCode,
        String country) {}
