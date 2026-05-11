package com.cinema.springcinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.springcinema.dto.PurchaseRequest;
import com.cinema.springcinema.dto.TicketDto;
import com.cinema.springcinema.service.TicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketDto> purchase(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PurchaseRequest request) {
        TicketDto ticket = ticketService.purchase(userId, request.screeningId(), request.seatRow(), request.seatNumber());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/me")
    public ResponseEntity<List<TicketDto>> getMyTickets(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        ticketService.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }
}
