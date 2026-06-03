package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cinema.springcinema.domain.Address;
import com.cinema.springcinema.domain.PaymentDetails;
import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.dto.AddressDto;
import com.cinema.springcinema.dto.PaymentDetailsDto;
import com.cinema.springcinema.dto.UserCreateRequest;
import com.cinema.springcinema.dto.UserDto;
import com.cinema.springcinema.dto.UserSearchRequest;
import com.cinema.springcinema.dto.UserUpdateRequest;
import com.cinema.springcinema.repository.UserRepository;
import com.querydsl.core.types.Predicate;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User("John Doe", "john@example.com", "encoded-password", User.Role.USER);
        user.setId(1L);
        Address address = new Address();
        address.setStreet("123 Main St");
        address.setCity("Springfield");
        address.setPostalCode("12345");
        address.setCountry("US");
        user.setAddress(address);
        PaymentDetails pd = new PaymentDetails();
        pd.setCardHolderName("John Doe");
        pd.setCardLastFour("4242");
        pd.setCardExpiry("12/28");
        user.setPaymentDetails(pd);
        userDto = UserDto.from(user);
    }

    @Test
    void givenPageable_whenFindAll_thenReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserDto> result = userService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("John Doe");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void givenEmptyPageable_whenFindAll_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserDto> result = userService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenExistingUserId_whenFindById_thenReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("John Doe");
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void givenNonExistentUserId_whenFindById_thenThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    void givenValidCreateRequest_whenCreate_thenReturnsUserDto() {
        UserCreateRequest request = new UserCreateRequest("Jane Doe", "jane@example.com", "password123", "USER",
                new AddressDto("456 Oak Ave", "Portland", "97201", "US"),
                new PaymentDetailsDto("Jane Doe", "1234", "06/29"));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserDto result = userService.create(request);

        assertThat(result.name()).isEqualTo("Jane Doe");
        assertThat(result.email()).isEqualTo("jane@example.com");
        assertThat(result.role()).isEqualTo("USER");
        assertThat(result.address()).isNotNull();
        assertThat(result.address().city()).isEqualTo("Portland");
        assertThat(result.paymentDetails()).isNotNull();
        assertThat(result.paymentDetails().cardLastFour()).isEqualTo("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenCreateRequestWithAdminRole_whenCreate_thenSetsAdminRole() {
        UserCreateRequest request = new UserCreateRequest("Admin User", "admin@example.com", "admin123", "ADMIN", null, null);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-admin-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(3L);
            return u;
        });

        UserDto result = userService.create(request);

        assertThat(result.role()).isEqualTo("ADMIN");
    }

    @Test
    void givenCreateRequestWithNullRole_whenCreate_thenDefaultsToUser() {
        UserCreateRequest request = new UserCreateRequest("No Role User", "norole@example.com", "pass123", null, null, null);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(4L);
            return u;
        });

        UserDto result = userService.create(request);

        assertThat(result.role()).isEqualTo("USER");
    }

    @Test
    void givenValidUpdateRequest_whenUpdate_thenReturnsUpdatedUser() {
        UserUpdateRequest request = new UserUpdateRequest("John Updated", "john.updated@example.com", null, "ADMIN",
                new AddressDto("789 New St", "Seattle", "98101", "US"), null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.update(1L, request);

        assertThat(result.name()).isEqualTo("John Updated");
        assertThat(result.email()).isEqualTo("john.updated@example.com");
        assertThat(result.role()).isEqualTo("ADMIN");
        assertThat(result.address().city()).isEqualTo("Seattle");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void givenUpdateRequestWithNewPassword_whenUpdate_thenEncodesPassword() {
        UserUpdateRequest request = new UserUpdateRequest("John", "john@example.com", "newPassword123", "USER", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(1L, request);

        verify(passwordEncoder).encode("newPassword123");
    }

    @Test
    void givenUpdateRequestWithBlankPassword_whenUpdate_thenDoesNotEncode() {
        UserUpdateRequest request = new UserUpdateRequest("John", "john@example.com", "  ", "USER", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.update(1L, request);

        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any());
    }

    @Test
    void givenNonExistentUserId_whenUpdate_thenThrowsException() {
        UserUpdateRequest request = new UserUpdateRequest("Nobody", "nobody@example.com", "pass", "USER", null, null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    void givenExistingUserId_whenDelete_thenDeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void givenNonExistentUserId_whenDelete_thenThrowsException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    void givenSearchByName_whenSearch_thenReturnsMatchingUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest("John", null, null, null, null, null);
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("John Doe");
    }

    @Test
    void givenSearchByCity_whenSearch_thenReturnsMatchingUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest(null, null, null, "Springfield", null, null);
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchByCountry_whenSearch_thenReturnsMatchingUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest(null, null, null, null, null, "US");
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchNoFilters_whenSearch_thenReturnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest(null, null, null, null, null, null);
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchWithNoResults_whenSearch_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest("NonExistent", null, null, null, null, null);
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenSearchByMultipleFields_whenSearch_thenReturnsMatchingUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchRequest request = new UserSearchRequest("John", null, null, "Springfield", null, "US");
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchWithPaging_whenSearch_thenReturnsCorrectPage() {
        Pageable pageable = PageRequest.of(1, 5);
        UserSearchRequest request = new UserSearchRequest(null, null, null, null, null, null);
        when(userRepository.findAll(any(Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<UserDto> result = userService.search(request, pageable);

        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
    }

    @Test
    void givenCreateRequestWithAddress_whenCreate_thenAddressIsSaved() {
        AddressDto addressDto = new AddressDto("100 Test Rd", "Testville", "00000", "Testland");
        UserCreateRequest request = new UserCreateRequest("Test User", "test@example.com", "pass", null, addressDto, null);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(5L);
            return u;
        });

        UserDto result = userService.create(request);

        assertThat(result.address()).isNotNull();
        assertThat(result.address().street()).isEqualTo("100 Test Rd");
        assertThat(result.address().city()).isEqualTo("Testville");
        assertThat(result.address().postalCode()).isEqualTo("00000");
        assertThat(result.address().country()).isEqualTo("Testland");
    }

    @Test
    void givenCreateRequestWithPaymentDetails_whenCreate_thenPaymentDetailsAreSaved() {
        PaymentDetailsDto pdDto = new PaymentDetailsDto("Test User", "9999", "01/30");
        UserCreateRequest request = new UserCreateRequest("Test User", "test@example.com", "pass", null, null, pdDto);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(6L);
            return u;
        });

        UserDto result = userService.create(request);

        assertThat(result.paymentDetails()).isNotNull();
        assertThat(result.paymentDetails().cardHolderName()).isEqualTo("Test User");
        assertThat(result.paymentDetails().cardLastFour()).isEqualTo("9999");
        assertThat(result.paymentDetails().cardExpiry()).isEqualTo("01/30");
    }
}
