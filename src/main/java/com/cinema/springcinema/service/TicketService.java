package com.cinema.springcinema.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Ticket;
import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.SeatDto;
import com.cinema.springcinema.dto.TicketDto;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.dto.ShowroomDto;
import com.cinema.springcinema.repository.ScreeningRepository;
import com.cinema.springcinema.repository.TicketRepository;
import com.cinema.springcinema.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ScreeningRepository screeningRepository;
    private final UserRepository userRepository;
    private final ScreeningService screeningService;

    public TicketService(TicketRepository ticketRepository, ScreeningRepository screeningRepository,
            UserRepository userRepository, ScreeningService screeningService) {
        this.ticketRepository = ticketRepository;
        this.screeningRepository = screeningRepository;
        this.userRepository = userRepository;
        this.screeningService = screeningService;
    }

    @Transactional
    public TicketDto purchase(Long userId, Long screeningId, String seatRow, Integer seatNumber) {
        if (ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(screeningId, seatRow, seatNumber)) {
            throw new IllegalArgumentException("Seat already taken");
        }

        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found: " + screeningId));

        if (screening.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot purchase ticket for past screening");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<SeatDto> seats = screeningService.getSeats(screeningId).seats();
        SeatDto seat = seats.stream()
                .filter(s -> s.seatRow().equals(seatRow) && s.seatNumber() == seatNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid seat"));

        if (!seat.available()) {
            throw new IllegalArgumentException("Seat is not available");
        }

        Ticket ticket = new Ticket(screening, user, seatRow, seatNumber, seat.price());
        return toDto(ticketRepository.save(ticket));
    }

    public List<TicketDto> getUserTickets(Long userId) {
        return ticketRepository.findByUserIdWithDetails(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public void cancel(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdWithDetails(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (!ticket.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own tickets");
        }

        if (ticket.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel ticket for past screening");
        }

        ticket.setStatus(Ticket.Status.CANCELLED);
        ticketRepository.save(ticket);
    }

    private TicketDto toDto(Ticket t) {
        Screening s = t.getScreening();
        return new TicketDto(
                t.getId(),
                new ScreeningDto(
                        s.getId(),
                        new MovieDto(s.getMovie().getId(), s.getMovie().getTitle(), s.getMovie().getDescription(),
                                s.getMovie().getDuration(), s.getMovie().getPosterUrl()),
                        new ShowroomDto(s.getShowroom().getId(), s.getShowroom().getName(), s.getShowroom().getRows(),
                                s.getShowroom().getSeatsPerRow(), s.getShowroom().getTotalSeats()),
                        s.getStartTime(),
                        s.getBasePrice()),
                t.getSeatRow(),
                t.getSeatNumber(),
                t.getPrice(),
                t.getStatus().name(),
                t.getPurchaseTime());
    }
}
