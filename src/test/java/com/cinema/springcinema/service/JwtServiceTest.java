package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.cinema.springcinema.domain.User;

import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = "mySecretKeyForJwtTokenGenerationThatIsLongEnoughForHS256";
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        jwtService.init();
    }

    @Test
    void givenValidUser_whenGenerateToken_thenTokenIsGenerated() {
        User user = new User("John Doe", "john@example.com", "encodedPassword", User.Role.USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void givenValidToken_whenExtractUserId_thenReturnsUserId() {
        User user = new User("Jane Doe", "jane@example.com", "encodedPassword", User.Role.USER);
        user.setId(42L);

        String token = jwtService.generateToken(user);
        Long extractedUserId = jwtService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(42L);
    }

    @Test
    void givenValidToken_whenExtractEmail_thenReturnsEmail() {
        User user = new User("Test User", "test@example.com", "encodedPassword", User.Role.ADMIN);
        user.setId(1L);

        String token = jwtService.generateToken(user);
        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    void givenValidToken_whenExtractRole_thenReturnsRole() {
        User user = new User("Admin User", "admin@example.com", "encodedPassword", User.Role.ADMIN);
        user.setId(1L);

        String token = jwtService.generateToken(user);
        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedRole).isEqualTo("ADMIN");
    }

    @Test
    void givenValidToken_whenExtractRoleForUser_thenReturnsUser() {
        User user = new User("Regular User", "user@example.com", "encodedPassword", User.Role.USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);
        String extractedRole = jwtService.extractRole(token);

        assertThat(extractedRole).isEqualTo("USER");
    }

    @Test
    void givenValidToken_whenIsTokenValid_thenReturnsTrue() {
        User user = new User("Valid User", "valid@example.com", "encodedPassword", User.Role.USER);
        user.setId(1L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void givenInvalidToken_whenIsTokenValid_thenReturnsFalse() {
        String invalidToken = "invalid.token.string";

        assertThat(jwtService.isTokenValid(invalidToken)).isFalse();
    }

    @Test
    void givenMalformedToken_whenIsTokenValid_thenReturnsFalse() {
        String malformedToken = "not.a.valid.jwt.token.at.all";

        assertThat(jwtService.isTokenValid(malformedToken)).isFalse();
    }

    @Test
    void givenDifferentUsers_whenGenerateToken_thenTokensAreDifferent() {
        User user1 = new User("User One", "user1@example.com", "pass", User.Role.USER);
        user1.setId(1L);
        User user2 = new User("User Two", "user2@example.com", "pass", User.Role.USER);
        user2.setId(2L);

        String token1 = jwtService.generateToken(user1);
        String token2 = jwtService.generateToken(user2);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void givenTokenWithDifferentUserId_whenExtractUserId_thenReturnsCorrectId() {
        User user = new User("Multi User", "multi@example.com", "pass", User.Role.USER);
        user.setId(999L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUserId(token)).isEqualTo(999L);
    }
}