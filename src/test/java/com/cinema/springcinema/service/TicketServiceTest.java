package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.domain.Ticket;
import com.cinema.springcinema.domain.User;
import com.cinema.springcinema.dto.ScreeningSeatsDto;
import com.cinema.springcinema.dto.SeatDto;
import com.cinema.springcinema.dto.TicketDto;
import com.cinema.springcinema.repository.ScreeningRepository;
import com.cinema.springcinema.repository.TicketRepository;
import com.cinema.springcinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ScreeningService screeningService;

    @InjectMocks
    private TicketService ticketService;

    private Movie movie;
    private Showroom showroom;
    private Screening screening;
    private User user;
    private User otherUser;
    private ScreeningSeatsDto screeningSeats;

    @BeforeEach
    void setUp() {
        movie = new Movie("Inception", "A mind-bending thriller", 148, "poster.jpg");
        movie.setId(1L);
        showroom = new Showroom("Main Hall", 10, 20);
        showroom.setId(1L);
        screening = new Screening(movie, showroom, LocalDateTime.now().plusDays(1), new BigDecimal("15.00"));
        screening.setId(1L);

        user = new User("John Doe", "john@example.com", "password", User.Role.USER);
        user.setId(1L);

        otherUser = new User("Jane Doe", "jane@example.com", "password", User.Role.USER);
        otherUser.setId(2L);

        screeningSeats = new ScreeningSeatsDto(
                new com.cinema.springcinema.dto.ScreeningDto(
                        screening.getId(),
                        new com.cinema.springcinema.dto.MovieDto(movie.getId(), movie.getTitle(), movie.getDescription(), movie.getDuration(), movie.getPosterUrl()),
                        new com.cinema.springcinema.dto.ShowroomDto(showroom.getId(), showroom.getName(), showroom.getRows(), showroom.getSeatsPerRow(), showroom.getTotalSeats()),
                        screening.getStartTime(),
                        screening.getBasePrice()),
                List.of(
                        new SeatDto("A", 1, true, new BigDecimal("18.00"), "PREMIUM"),
                        new SeatDto("A", 2, true, new BigDecimal("18.00"), "PREMIUM"),
                        new SeatDto("B", 1, true, new BigDecimal("15.00"), "STANDARD")
                ));
    }

    @Test
    void givenValidPurchaseRequest_whenPurchase_thenReturnsTicketDto() {
        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "A", 1)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningService.getSeats(1L)).thenReturn(screeningSeats);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TicketDto result = ticketService.purchase(1L, 1L, "A", 1);

        assertThat(result.seatRow()).isEqualTo("A");
        assertThat(result.seatNumber()).isEqualTo(1);
    }

    @Test
    void givenAlreadyBookedSeat_whenPurchase_thenThrowsException() {
        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "A", 1)).thenReturn(true);

        assertThatThrownBy(() -> ticketService.purchase(1L, 1L, "A", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seat already taken");
    }

    @Test
    void givenNonExistentScreeningId_whenPurchase_thenThrowsException() {
        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(999L, "A", 1)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(1L, 999L, "A", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Screening not found: 999");
    }

    @Test
    void givenPastScreening_whenPurchase_thenThrowsException() {
        Screening pastScreening = new Screening(movie, showroom, LocalDateTime.now().minusDays(1), new BigDecimal("15.00"));
        pastScreening.setId(1L);

        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "A", 1)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pastScreening));

        assertThatThrownBy(() -> ticketService.purchase(1L, 1L, "A", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot purchase ticket for past screening");
    }

    @Test
    void givenNonExistentUserId_whenPurchase_thenThrowsException() {
        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "A", 1)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.purchase(999L, 1L, "A", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    void givenInvalidSeatRowAndNumber_whenPurchase_thenThrowsException() {
        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "Z", 99)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningService.getSeats(1L)).thenReturn(screeningSeats);

        assertThatThrownBy(() -> ticketService.purchase(1L, 1L, "Z", 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid seat");
    }

    @Test
    void givenUnavailableSeat_whenPurchase_thenThrowsException() {
        ScreeningSeatsDto seatsWithUnavailable = new ScreeningSeatsDto(screeningSeats.screening(), List.of(
                new SeatDto("A", 1, false, new BigDecimal("18.00"), "PREMIUM")
        ));

        when(ticketRepository.existsByScreeningIdAndSeatRowAndSeatNumber(1L, "A", 1)).thenReturn(false);
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(screeningService.getSeats(1L)).thenReturn(seatsWithUnavailable);

        assertThatThrownBy(() -> ticketService.purchase(1L, 1L, "A", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seat is not available");
    }

    @Test
    void givenUserId_whenGetUserTickets_thenReturnsTickets() {
        Ticket ticket = new Ticket(screening, user, "A", 1, new BigDecimal("18.00"));
        ticket.setId(1L);

        when(ticketRepository.findByUserIdWithDetails(1L)).thenReturn(List.of(ticket));

        List<TicketDto> result = ticketService.getUserTickets(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void givenUserWithNoTickets_whenGetUserTickets_thenReturnsEmptyList() {
        when(ticketRepository.findByUserIdWithDetails(1L)).thenReturn(List.of());

        List<TicketDto> result = ticketService.getUserTickets(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void givenValidTicketId_whenCancel_thenUpdatesTicketStatus() {
        Ticket ticket = new Ticket(screening, user, "A", 1, new BigDecimal("18.00"));
        ticket.setId(1L);

        when(ticketRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.cancel(1L, 1L);

        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CANCELLED);
    }

    @Test
    void givenNonExistentTicketId_whenCancel_thenThrowsException() {
        when(ticketRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancel(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ticket not found: 999");
    }

    @Test
    void givenTicketOwnedByOtherUser_whenCancel_thenThrowsAccessDeniedException() {
        Ticket ticket = new Ticket(screening, otherUser, "A", 1, new BigDecimal("18.00"));
        ticket.setId(1L);

        when(ticketRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel(1L, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You can only cancel your own tickets");
    }

    @Test
    void givenPastScreeningTicket_whenCancel_thenThrowsException() {
        Screening pastScreening = new Screening(movie, showroom, LocalDateTime.now().minusDays(1), new BigDecimal("15.00"));
        pastScreening.setId(1L);
        Ticket ticket = new Ticket(pastScreening, user, "A", 1, new BigDecimal("18.00"));
        ticket.setId(1L);

        when(ticketRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancel(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot cancel ticket for past screening");
    }

    @Test
    void givenAlreadyCancelledTicket_whenCancel_thenUpdatesStatusAgain() {
        Ticket ticket = new Ticket(screening, user, "A", 1, new BigDecimal("18.00"));
        ticket.setId(1L);
        ticket.setStatus(Ticket.Status.CANCELLED);

        when(ticketRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.cancel(1L, 1L);

        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CANCELLED);
    }
}