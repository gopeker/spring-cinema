package com.cinema.springcinema.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void givenValidParameters_whenCreateUser_thenUserIsCreated() {
        User user = new User("John Doe", "john@example.com", "password123", User.Role.USER);

        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        assertThat(user.getId()).isNull();
    }

    @Test
    void givenAdminRole_whenCreateUser_thenUserHasAdminRole() {
        User user = new User("Admin User", "admin@example.com", "adminpass", User.Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void givenUserRole_whenCreateUser_thenUserHasUserRole() {
        User user = new User("Jane Doe", "jane@example.com", "password", User.Role.USER);

        assertThat(user.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void whenSetId_thenIdIsUpdated() {
        User user = new User();
        user.setId(1L);

        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void whenSetName_thenNameIsUpdated() {
        User user = new User();
        user.setName("Updated Name");

        assertThat(user.getName()).isEqualTo("Updated Name");
    }

    @Test
    void whenSetEmail_thenEmailIsUpdated() {
        User user = new User();
        user.setEmail("newemail@example.com");

        assertThat(user.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    void whenSetPassword_thenPasswordIsUpdated() {
        User user = new User();
        user.setPassword("newpassword");

        assertThat(user.getPassword()).isEqualTo("newpassword");
    }

    @Test
    void whenSetRole_thenRoleIsUpdated() {
        User user = new User();
        user.setRole(User.Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void givenUserRole_whenCheckIsUser_thenReturnsTrue() {
        User user = new User("Test", "test@example.com", "pass", User.Role.USER);

        assertThat(user.getRole()).isEqualTo(User.Role.USER);
    }

    @Test
    void givenAdminRole_whenCheckIsAdmin_thenReturnsTrue() {
        User user = new User("Admin", "admin@example.com", "pass", User.Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
    }
}