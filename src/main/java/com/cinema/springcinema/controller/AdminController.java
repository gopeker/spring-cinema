package com.cinema.springcinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.MovieCreateRequest;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.dto.ScreeningCreateRequest;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.service.MovieService;
import com.cinema.springcinema.service.ScreeningService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MovieService movieService;
    private final ScreeningService screeningService;

    public AdminController(MovieService movieService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
    }

    @GetMapping("/movies")
    public ResponseEntity<List<MovieDto>> listMovies() {
        return ResponseEntity.ok(movieService.findAll());
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

    @GetMapping("/screenings")
    public ResponseEntity<List<ScreeningDto>> listScreenings() {
        return ResponseEntity.ok(screeningService.findAll());
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
}
