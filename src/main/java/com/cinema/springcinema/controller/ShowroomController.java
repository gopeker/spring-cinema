package com.cinema.springcinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.ShowroomDto;
import com.cinema.springcinema.service.ShowroomService;

@RestController
@RequestMapping("/api/showrooms")
public class ShowroomController {

    private final ShowroomService showroomService;

    public ShowroomController(ShowroomService showroomService) {
        this.showroomService = showroomService;
    }

    @GetMapping
    public ResponseEntity<List<ShowroomDto>> findAll() {
        return ResponseEntity.ok(showroomService.findAll());
    }
}
