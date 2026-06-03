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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.domain.Ticket;
import com.cinema.springcinema.dto.ScreeningCreateRequest;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.ScreeningSeatsDto;
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
        when(screeningRepository.findAll(any(Sort.class))).thenReturn(List.of(screening));

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

        ScreeningSeatsDto result = screeningService.getSeats(1L);

        assertThat(result.seats()).hasSize(200);
        assertThat(result.seats().get(0).seatRow()).isEqualTo("A");
        assertThat(result.seats().get(0).seatNumber()).isEqualTo(1);
        assertThat(result.seats().get(0).available()).isTrue();
        assertThat(result.screening().id()).isEqualTo(1L);
    }

    @Test
    void givenSoldSeat_whenGetSeats_thenSeatIsNotAvailable() {
        Ticket soldTicket = new Ticket(screening, null, "A", 1, new BigDecimal("18.00"));
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of(soldTicket));

        ScreeningSeatsDto result = screeningService.getSeats(1L);

        SeatDto a1 = result.seats().stream().filter(s -> s.seatRow().equals("A") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(a1).isNotNull();
        assertThat(a1.available()).isFalse();
    }

    @Test
    void whenGetSeats_thenFrontRowsArePremium() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        ScreeningSeatsDto result = screeningService.getSeats(1L);

        SeatDto frontRowSeat = result.seats().stream().filter(s -> s.seatRow().equals("A") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(frontRowSeat).isNotNull();
        assertThat(frontRowSeat.tier()).isEqualTo("PREMIUM");
    }

    @Test
    void whenGetSeats_thenBackRowsAreEconomy() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        ScreeningSeatsDto result = screeningService.getSeats(1L);

        SeatDto backRowSeat = result.seats().stream().filter(s -> s.seatRow().equals("J") && s.seatNumber() == 1).findFirst().orElse(null);
        assertThat(backRowSeat).isNotNull();
        assertThat(backRowSeat.tier()).isEqualTo("ECONOMY");
    }

    @Test
    void whenGetSeats_thenMiddleRowsAreStandard() {
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(ticketRepository.findByScreeningIdAndStatus(1L, Ticket.Status.CONFIRMED)).thenReturn(List.of());

        ScreeningSeatsDto result = screeningService.getSeats(1L);

        SeatDto middleRowSeat = result.seats().stream().filter(s -> s.seatRow().equals("E") && s.seatNumber() == 1).findFirst().orElse(null);
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
        when(screeningRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(screening));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showroomRepository.findById(1L)).thenReturn(Optional.of(showroom));
        when(screeningRepository.save(any(Screening.class))).thenReturn(screening);

        ScreeningDto result = screeningService.update(1L, request);

        verify(screeningRepository).save(any(Screening.class));
    }

    @Test
    void givenNonExistentScreeningId_whenUpdate_thenThrowsException() {
        ScreeningCreateRequest request = new ScreeningCreateRequest(1L, 1L, LocalDateTime.now().plusDays(2), new BigDecimal("15.00"));
        when(screeningRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

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

    @Test
    void givenPageable_whenFindAllPageable_thenReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(pageable)).thenReturn(page);

        Page<ScreeningDto> result = screeningService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void givenEmptyPageable_whenFindAllPageable_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(), pageable, 0);
        when(screeningRepository.findAll(pageable)).thenReturn(page);

        Page<ScreeningDto> result = screeningService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenSecondPage_whenFindAllPageable_thenReturnsCorrectPage() {
        Screening screening2 = new Screening(movie, showroom, LocalDateTime.now().plusDays(5), new BigDecimal("20.00"));
        screening2.setId(2L);
        Pageable pageable = PageRequest.of(1, 1);
        Page<Screening> page = new PageImpl<>(List.of(screening2), pageable, 2);
        when(screeningRepository.findAll(pageable)).thenReturn(page);

        Page<ScreeningDto> result = screeningService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(2L);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
    }

    @Test
    void givenMovieId_whenSearch_thenReturnsMatchingScreenings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(1L, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).movie().title()).isEqualTo("Inception");
    }

    @Test
    void givenPriceRange_whenSearch_thenReturnsMatchingScreenings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(null, null, null, new BigDecimal("10.00"), new BigDecimal("20.00"), pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenDateRange_whenSearch_thenReturnsMatchingScreenings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(null, LocalDateTime.now(), LocalDateTime.now().plusDays(7), null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenAllFilters_whenSearch_thenReturnsMatchingScreenings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenNoSearchResults_whenSearch_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(), pageable, 0);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(999L, null, null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenAllNullFilters_whenSearch_thenReturnsAllScreenings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchWithPaging_whenSearch_thenReturnsCorrectPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Screening> page = new PageImpl<>(List.of(screening), pageable, 1);
        when(screeningRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ScreeningDto> result = screeningService.search(null, null, null, null, null, pageable);

        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}