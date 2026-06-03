package com.cinema.springcinema.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.MovieCreateRequest;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.dto.ScreeningCreateRequest;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.UserCreateRequest;
import com.cinema.springcinema.dto.UserDto;
import com.cinema.springcinema.dto.UserSearchRequest;
import com.cinema.springcinema.dto.UserUpdateRequest;
import com.cinema.springcinema.service.MovieService;
import com.cinema.springcinema.service.ScreeningService;
import com.cinema.springcinema.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final UserService userService;

    public AdminController(MovieService movieService, ScreeningService screeningService, UserService userService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
        this.userService = userService;
    }

    // ── Movies ──────────────────────────────────────────────────────────────

    @GetMapping("/movies")
    public ResponseEntity<Page<MovieDto>> listMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        if (search != null) {
            return ResponseEntity.ok(movieService.searchByName(search, pageable));
        }
        if (title != null || description != null || minDuration != null || maxDuration != null) {
            return ResponseEntity.ok(movieService.search(title, description, minDuration, maxDuration, pageable));
        }
        return ResponseEntity.ok(movieService.findAll(pageable));
    }

    @PostMapping("/movies")
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        return ResponseEntity.ok(movieService.create(request));
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieCreateRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Screenings ───────────────────────────────────────────────────────────

    @GetMapping("/screenings")
    public ResponseEntity<Page<ScreeningDto>> listScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        if (movieId != null || from != null || to != null || minPrice != null || maxPrice != null) {
            return ResponseEntity.ok(screeningService.search(movieId, from, to, minPrice, maxPrice, pageable));
        }
        return ResponseEntity.ok(screeningService.findAll(pageable));
    }

    @PostMapping("/screenings")
    public ResponseEntity<ScreeningDto> createScreening(@Valid @RequestBody ScreeningCreateRequest request) {
        return ResponseEntity.ok(screeningService.create(request));
    }

    @PutMapping("/screenings/{id}")
    public ResponseEntity<ScreeningDto> updateScreening(@PathVariable Long id, @Valid @RequestBody ScreeningCreateRequest request) {
        return ResponseEntity.ok(screeningService.update(id, request));
    }

    @DeleteMapping("/screenings/{id}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long id) {
        screeningService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Users ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> listUsers(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @PostMapping("/users/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestBody UserSearchRequest request,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.search(request, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
