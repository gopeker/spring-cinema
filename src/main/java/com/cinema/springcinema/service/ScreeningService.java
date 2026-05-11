package com.cinema.springcinema.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.domain.Screening;
import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.domain.Ticket;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.dto.ScreeningCreateRequest;
import com.cinema.springcinema.dto.ScreeningDto;
import com.cinema.springcinema.dto.SeatDto;
import com.cinema.springcinema.dto.ShowroomDto;
import com.cinema.springcinema.repository.MovieRepository;
import com.cinema.springcinema.repository.ScreeningRepository;
import com.cinema.springcinema.repository.ShowroomRepository;
import com.cinema.springcinema.repository.TicketRepository;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final ShowroomRepository showroomRepository;
    private final TicketRepository ticketRepository;

    public ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository,
            ShowroomRepository showroomRepository, TicketRepository ticketRepository) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.showroomRepository = showroomRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<ScreeningDto> findAll() {
        return screeningRepository.findUpcoming(LocalDateTime.now()).stream().map(this::toDto).toList();
    }

    public List<ScreeningDto> findByMovieId(Long movieId) {
        return screeningRepository.findUpcomingByMovieId(movieId, LocalDateTime.now())
                .stream().map(this::toDto).toList();
    }

    public List<ScreeningDto> findByDate(LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(LocalTime.MAX);
        return screeningRepository.findByDateRange(from, to).stream().map(this::toDto).toList();
    }

    public ScreeningDto findById(Long id) {
        return screeningRepository.findByIdWithDetails(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found: " + id));
    }

    public List<SeatDto> getSeats(Long screeningId) {
        Screening screening = screeningRepository.findByIdWithDetails(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found: " + screeningId));

        Showroom showroom = screening.getShowroom();
        if (showroom == null) {
            throw new IllegalArgumentException("Screening has no showroom: " + screeningId);
        }
        List<Ticket> sold = ticketRepository.findByScreeningIdAndStatus(screeningId, Ticket.Status.CONFIRMED);

        List<SeatDto> seats = new ArrayList<>();
        String[] rows = generateRowLabels(showroom.getRows());
        int frontPremiumRows = Math.min(2, showroom.getRows());
        int backDiscountRows = Math.min(2, showroom.getRows());

        for (String row : rows) {
            final String currentRow = row;
            final int rowIndex = getRowIndex(currentRow, rows);
            for (int num = 1; num <= showroom.getSeatsPerRow(); num++) {
                final int seatNum = num;
                boolean soldSeat = sold.stream()
                        .anyMatch(t -> t.getSeatRow().equals(currentRow) && t.getSeatNumber() == seatNum);
                BigDecimal price = calculatePrice(screening.getBasePrice(), rowIndex, rows.length,
                        frontPremiumRows, backDiscountRows);
                String tier = getTier(rowIndex, rows.length, frontPremiumRows, backDiscountRows);
                seats.add(new SeatDto(currentRow, seatNum, !soldSeat, price, tier));
            }
        }
        return seats;
    }

    public ScreeningDto create(ScreeningCreateRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + request.movieId()));
        Showroom showroom = showroomRepository.findById(request.showroomId())
                .orElseThrow(() -> new IllegalArgumentException("Showroom not found: " + request.showroomId()));
        Screening screening = new Screening(movie, showroom, request.startTime(), request.basePrice());
        return toDto(screeningRepository.save(screening));
    }

    public ScreeningDto update(Long id, ScreeningCreateRequest request) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screening not found: " + id));
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + request.movieId()));
        Showroom showroom = showroomRepository.findById(request.showroomId())
                .orElseThrow(() -> new IllegalArgumentException("Showroom not found: " + request.showroomId()));
        screening.setMovie(movie);
        screening.setShowroom(showroom);
        screening.setStartTime(request.startTime());
        screening.setBasePrice(request.basePrice());
        return toDto(screeningRepository.save(screening));
    }

    public void delete(Long id) {
        if (!screeningRepository.existsById(id)) {
            throw new IllegalArgumentException("Screening not found: " + id);
        }
        screeningRepository.deleteById(id);
    }

    private ScreeningDto toDto(Screening s) {
        Movie m = s.getMovie();
        Showroom sw = s.getShowroom();
        if (m == null || sw == null) {
            throw new IllegalArgumentException("Screening has missing movie or showroom: " + s.getId());
        }
        return new ScreeningDto(
                s.getId(),
                new MovieDto(m.getId(), m.getTitle(), m.getDescription(), m.getDuration(), m.getPosterUrl()),
                new ShowroomDto(sw.getId(), sw.getName(), sw.getRows(), sw.getSeatsPerRow(), sw.getTotalSeats()),
                s.getStartTime(),
                s.getBasePrice());
    }

    private String[] generateRowLabels(int rows) {
        String[] labels = new String[rows];
        for (int i = 0; i < rows; i++) {
            labels[i] = String.valueOf((char) ('A' + i));
        }
        return labels;
    }

    private int getRowIndex(String row, String[] allRows) {
        for (int i = 0; i < allRows.length; i++) {
            if (allRows[i].equals(row)) return i;
        }
        return 0;
    }

    private BigDecimal calculatePrice(BigDecimal basePrice, int rowIndex, int totalRows,
            int frontPremiumRows, int backDiscountRows) {
        if (rowIndex < frontPremiumRows) {
            return basePrice.add(new BigDecimal("3.00"));
        } else if (rowIndex >= totalRows - backDiscountRows) {
            return basePrice.subtract(new BigDecimal("2.00"));
        }
        return basePrice;
    }

    private String getTier(int rowIndex, int totalRows, int frontPremiumRows, int backDiscountRows) {
        if (rowIndex < frontPremiumRows) return "PREMIUM";
        if (rowIndex >= totalRows - backDiscountRows) return "ECONOMY";
        return "STANDARD";
    }
}
