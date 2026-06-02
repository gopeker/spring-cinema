package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.domain.Ticket;
import com.cinema.springcinema.dto.ScreeningCreateRequest;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.SeatDto;
import com.cinema.springcinema.repository.MovieRepository;
import com.cinema.springcinema.repository.ScreeningRepository;
import com.cinema.springcinema.repository.ShowroomRepository;
import com.cinema.springcinema.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ShowroomRepository showroomRepository;
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ScreeningService screeningService;

    private Movie movie;
    private Showroom showroom;
    private Screening screening;

    @BeforeEach
    void setUp() {
        movie = new Movie("Inception", "A mind-bending thriller", 148, "poster.jpg");
        movie.setId(1L);
        showroom = new Showroom("Main Hall", 10, 20);
        showroom.setId(1L);
        screening = new Screening(movie, showroom, LocalDateTime.now().plusDays(1), new BigDecimal("15.00"));
        screening.setId(1L);
    }

    @Test
    void whenFindAll_thenReturnsUpcomingScreenings() {
        when(screeningRepository.findUpcoming(any(LocalDateTime.class))).thenReturn(List.of(screening));

        List<ScreeningDto> result = screeningService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

@Test
    void givenMovieId_whenFindByMovieId_thenReturnsScreeningsForMovie() {
        when(screeningRepository.findUpcomingByMovieId(eq(1L), any(LocalDateTime.class))).thenReturn(List.of(screening));

        List<ScreeningDto> result = screeningService.findByMovieId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void givenDate_whenFindByDate_thenReturnsScreeningsForDate() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(screeningRepository.findByDateRange(any(), any())).thenReturn(List.of(screening));

        List<ScreeningDto> result = screeningService.findByDate(date);

        assertThat(result).hasSize(1);
    }

    @Test
    void givenScreeningId_whenFindById_thenReturnsScreening() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));

        ScreeningDto result = screeningService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void givenNonExistentScreeningId_whenFindById_thenThrowsException() {
        when(screeningRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Screening not found: 999");
    }

    @Test
    void givenScreeningId_whenGetSeats_thenReturnsSeats() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        List<SeatDto> result = screeningService.getSeats(1L);

        assertThat(result).hasSize(200);
        assertThat(result.get(0).seatRow()).isEqualTo("A");
        assertThat(result.get(0).seatNumber()).isEqualTo(1);
        assertThat(result.get(0).available()).isTrue();
    }

    @Test
    void givenSoldSeat_whenGetSeats_thenSeatIsNotAvailable() {
        Ticket soldTicket = new Ticket(screening, null, "A", 1, new BigDecimal("18.00"));
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of(soldTicket));

        List<SeatDto> result = screeningService.getSeats(1L);

        SeatDto a1 = result.stream().filter(s -> s.seatRow().equals("A") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(a1).isNotNull();
        assertThat(a1.available()).isFalse();
    }

    @Test
    void whenGetSeats_thenFrontRowsArePremium() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        List<SeatDto> result = screeningService.getSeats(1L);

        SeatDto frontRowSeat = result.stream().filter(s -> s.seatRow().equals("A") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(frontRowSeat).isNotNull();
        assertThat(frontRowSeat.tier()).isEqualTo("PREMIUM");
    }

    @Test
    void whenGetSeats_thenBackRowsAreEconomy() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        List<SeatDto> result = screeningService.getSeats(1L);

        SeatDto backRowSeat = result.stream().filter(s -> s.seatRow().equals("J") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(backRowSeat).isNotNull();
        assertThat(backRowSeat.tier()).isEqualTo("ECONOMY");
    }

    @Test
    void whenGetSeats_thenMiddleRowsAreStandard() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        List<SeatDto> result = screeningService.getSeats(1L);

        SeatDto middleRowSeat = result.stream().filter(s -> s.seatRow().equals("E") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(middleRowSeat).isNotNull();
        assertThat(middleRowSeat.tier()).isEqualTo("STANDARD");
    }

    @Test
    void givenValidCreateRequest_whenCreate_thenReturnsScreeningDto() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(1L, 1L, LocalDateTime.now().plusDays(2), new BigDecimal("15.00"));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showroomRepository.findById(1L)).thenReturn(Optional.of(showroom));
        when(screeningRepository.save(any(Screening.class))).thenReturn(screening);

        ScreeningDto result = screeningService.create(request);

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    void givenNonExistentMovieId_whenCreate_thenThrowsException() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(999L, 1L, LocalDateTime.now().plusDays(2), new BigDecimal("15.00"));
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Movie not found: 999");
    }

    @Test
    void givenNonExistentShowroomId_whenCreate_thenThrowsException() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(1L, 999L, LocalDateTime.now().plusDays(2), new BigDecimal("15.00"));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showroomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Showroom not found: 999");
    }

    @Test
    void givenValidUpdateRequest_whenUpdate_thenReturnsUpdatedScreening() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(1L, 1L, LocalDateTime.now().plusDays(3), new BigDecimal("18.00"));
        when(screeningRepository.findById(1L)).thenReturn(Optional.of(screening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showroomRepository.findById(1L)).thenReturn(Optional.of(showroom));
        when(screeningRepository.save(any(Screening.class))).thenReturn(screening);

        ScreeningDto result = screeningService.update(1L, request);

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    void givenNonExistentScreeningId_whenUpdate_thenThrowsException() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(1L, 1L, LocalDateTime.now().plusDays(2), new BigDecimal("15.00"));
        when(screeningRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.update(999L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Screening not found: 999");
    }

    @Test
    void givenExistingScreeningId_whenDelete_thenDeletesScreening() {
        when(screeningRepository.existsById(1L)).thenReturn(true);
        doNothing().when(screeningRepository).deleteById(1L);

        screeningService.delete(1L);

        verify(screeningRepository).deleteById(1L);
    }

    @Test
    void givenNonExistentScreeningId_whenDelete_thenThrowsException() {
        when(screeningRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> screeningService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Screening not found: 999");
    }
}