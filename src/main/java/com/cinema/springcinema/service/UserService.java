package com.cinema.springcinema.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cinema.springcinema.domain.Address;
import com.cinema.springcinema.domain.PaymentDetails;
import com.cinema.springcinema.domain.QUser;
import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.dto.AddressDto;
import com.cinema.springcinema.dto.PaymentDetailsDto;
import com.cinema.springcinema.dto.UserCreateRequest;
import com.cinema.springcinema.dto.UserDto;
import com.cinema.springcinema.dto.UserSearchRequest;
import com.cinema.springcinema.dto.UserUpdateRequest;
import com.cinema.springcinema.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::from)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public UserDto create(UserCreateRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.role() != null) {
            user.setRole(User.Role.valueOf(request.role().toUpperCase()));
        } else {
            user.setRole(User.Role.USER);
        }
        applyAddress(user, request.address());
        applyPaymentDetails(user, request.paymentDetails());
        return UserDto.from(userRepository.save(user));
    }

    public UserDto update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setName(request.name());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(User.Role.valueOf(request.role().toUpperCase()));
        }
        applyAddress(user, request.address());
        applyPaymentDetails(user, request.paymentDetails());
        return UserDto.from(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    public Page<UserDto> search(UserSearchRequest request, Pageable pageable) {
        QUser qUser = QUser.user;
        BooleanBuilder predicate = new BooleanBuilder();

        if (request.name() != null && !request.name().isBlank()) {
            predicate.and(qUser.name.containsIgnoreCase(request.name()));
        }
        if (request.email() != null && !request.email().isBlank()) {
            predicate.and(qUser.email.containsIgnoreCase(request.email()));
        }
        if (request.street() != null && !request.street().isBlank()) {
            predicate.and(qUser.address().street.containsIgnoreCase(request.street()));
        }
        if (request.city() != null && !request.city().isBlank()) {
            predicate.and(qUser.address().city.containsIgnoreCase(request.city()));
        }
        if (request.postalCode() != null && !request.postalCode().isBlank()) {
            predicate.and(qUser.address().postalCode.containsIgnoreCase(request.postalCode()));
        }
        if (request.country() != null && !request.country().isBlank()) {
            predicate.and(qUser.address().country.containsIgnoreCase(request.country()));
        }

        return userRepository.findAll(predicate, pageable).map(UserDto::from);
    }

    private void applyAddress(User user, AddressDto dto) {
        if (dto == null) return;
        Address addr = user.getAddress() != null ? user.getAddress() : new Address();
        addr.setStreet(dto.street());
        addr.setCity(dto.city());
        addr.setPostalCode(dto.postalCode());
        addr.setCountry(dto.country());
        user.setAddress(addr);
    }

    private void applyPaymentDetails(User user, PaymentDetailsDto dto) {
        if (dto == null) return;
        PaymentDetails pd = user.getPaymentDetails() != null ? user.getPaymentDetails() : new PaymentDetails();
        pd.setCardHolderName(dto.cardHolderName());
        pd.setCardLastFour(dto.cardLastFour());
        pd.setCardExpiry(dto.cardExpiry());
        user.setPaymentDetails(pd);
    }
}
