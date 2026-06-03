package com.cinema.springcinema.dto;

import com.cinema.springcinema.domain.User;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        AddressDto address,
        PaymentDetailsDto paymentDetails) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getAddress() == null ? null : new AddressDto(
                        user.getAddress().getStreet(),
                        user.getAddress().getCity(),
                        user.getAddress().getPostalCode(),
                        user.getAddress().getCountry()),
                user.getPaymentDetails() == null ? null : new PaymentDetailsDto(
                        user.getPaymentDetails().getCardHolderName(),
                        user.getPaymentDetails().getCardLastFour(),
                        user.getPaymentDetails().getCardExpiry()));
    }
}
