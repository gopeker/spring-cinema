package com.cinema.springcinema.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.SeatDto;
import com.cinema.springcinema.service.ScreeningService;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @GetMapping
    public ResponseEntity<List<ScreeningDto>> findAll(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) LocalDate date) {
        if (movieId != null) {
            return ResponseEntity.ok(screeningService.findByMovieId(movieId));
        }
        if (date != null) {
            return ResponseEntity.ok(screeningService.findByDate(date));
        }
        return ResponseEntity.ok(screeningService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreeningDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(screeningService.findById(id));
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDto>> getSeats(@PathVariable Long id) {
        return ResponseEntity.ok(screeningService.getSeats(id));
    }
}
