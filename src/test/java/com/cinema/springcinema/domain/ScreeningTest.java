package com.cinema.springcinema.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ScreeningTest {

    @Test
    void givenValidParameters_whenCreateScreening_thenScreeningIsCreated() {
        Movie movie = new Movie("Test Movie", "Description", 120, null);
        Showroom showroom = new Showroom("Hall 1", 10, 20);
        LocalDateTime startTime = LocalDateTime.of(2024, 6, 15, 18, 0);
        BigDecimal basePrice = new BigDecimal("15.00");

        Screening screening = new Screening(movie, showroom, startTime, basePrice);

        assertThat(screening.getMovie()).isEqualTo(movie);
        assertThat(screening.getShowroom()).isEqualTo(showroom);
        assertThat(screening.getStartTime()).isEqualTo(startTime);
        assertThat(screening.getBasePrice()).isEqualByComparingTo(basePrice);
        assertThat(screening.getId()).isNull();
    }

    @Test
    void whenSetId_thenIdIsUpdated() {
        Screening screening = new Screening();
        screening.setId(1L);

        assertThat(screening.getId()).isEqualTo(1L);
    }

    @Test
    void whenSetMovie_thenMovieIsUpdated() {
        Screening screening = new Screening();
        Movie movie = new Movie("New Movie", null, 90, null);
        screening.setMovie(movie);

        assertThat(screening.getMovie()).isEqualTo(movie);
    }

    @Test
    void whenSetShowroom_thenShowroomIsUpdated() {
        Screening screening = new Screening();
        Showroom showroom = new Showroom("New Hall", 5, 10);
        screening.setShowroom(showroom);

        assertThat(screening.getShowroom()).isEqualTo(showroom);
    }

    @Test
    void whenSetStartTime_thenStartTimeIsUpdated() {
        Screening screening = new Screening();
        LocalDateTime newTime = LocalDateTime.of(2025, 1, 1, 20, 0);
        screening.setStartTime(newTime);

        assertThat(screening.getStartTime()).isEqualTo(newTime);
    }

    @Test
    void whenSetBasePrice_thenBasePriceIsUpdated() {
        Screening screening = new Screening();
        BigDecimal newPrice = new BigDecimal("25.50");
        screening.setBasePrice(newPrice);

        assertThat(screening.getBasePrice()).isEqualByComparingTo(newPrice);
    }

    @Test
    void whenSetCreatedAt_thenCreatedAtIsUpdated() {
        Screening screening = new Screening();
        LocalDateTime createdAt = LocalDateTime.now();
        screening.setCreatedAt(createdAt);

        assertThat(screening.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void givenDifferentBasePrices_whenCreateScreening_thenPriceIsStoredCorrectly() {
        Movie movie = new Movie("Movie", null, 100, null);
        Showroom showroom = new Showroom("Hall", 5, 10);

        Screening screening1 = new Screening(movie, showroom, LocalDateTime.now(), new BigDecimal("10.00"));
        Screening screening2 = new Screening(movie, showroom, LocalDateTime.now(), new BigDecimal("20.00"));
        Screening screening3 = new Screening(movie, showroom, LocalDateTime.now(), new BigDecimal("12.50"));

        assertThat(screening1.getBasePrice()).isEqualByComparingTo("10.00");
        assertThat(screening2.getBasePrice()).isEqualByComparingTo("20.00");
        assertThat(screening3.getBasePrice()).isEqualByComparingTo("12.50");
    }
}