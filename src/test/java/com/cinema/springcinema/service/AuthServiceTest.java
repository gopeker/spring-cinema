package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.dto.AuthResponse;
import com.cinema.springcinema.dto.LoginRequest;
import com.cinema.springcinema.dto.RegisterRequest;
import com.cinema.springcinema.repository.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        loginRequest = new LoginRequest("john@example.com", "password123");
        savedUser = new User("John Doe", "john@example.com", "encodedPassword", User.Role.USER);
        savedUser.setId(1L);
    }

    @Test
    void givenValidRegisterRequest_whenRegister_thenReturnsAuthResponse() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void givenExistingEmail_whenRegister_thenThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenValidLoginRequest_whenLogin_thenReturnsAuthResponse() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
    }

    @Test
    void givenNonExistentEmail_whenLogin_thenThrowsBadCredentialsException() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("nonexistent@example.com", "password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void givenWrongPassword_whenLogin_thenThrowsBadCredentialsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void givenUserWithAdminRole_whenLogin_thenReturnsAdminRoleInResponse() {
        User adminUser = new User("Admin", "admin@example.com", "encodedPassword", User.Role.ADMIN);
        adminUser.setId(2L);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(adminUser)).thenReturn("admin-token");

        LoginRequest adminRequest = new LoginRequest("admin@example.com", "password123");
        AuthResponse response = authService.login(adminRequest);

        assertThat(response.role()).isEqualTo("ADMIN");
    }

    @Test
    void givenMultipleRegisterCallsWithSameEmail_whenRegister_thenThrowsException() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true, false);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already in use");
    }

    @Test
    void givenEmptyPassword_whenRegister_thenPasswordIsEncoded() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("")).thenReturn("encodedEmptyPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("token");

        RegisterRequest emptyPasswordRequest = new RegisterRequest("John", "john@example.com", "");
        authService.register(emptyPasswordRequest);

        verify(passwordEncoder).encode("");
    }
}