package com.cinema.springcinema.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShowroomTest {

    @Test
    void givenValidParameters_whenCreateShowroom_thenShowroomIsCreated() {
        Showroom showroom = new Showroom("Main Hall", 10, 20);

        assertThat(showroom.getName()).isEqualTo("Main Hall");
        assertThat(showroom.getRows()).isEqualTo(10);
        assertThat(showroom.getSeatsPerRow()).isEqualTo(20);
        assertThat(showroom.getId()).isNull();
    }

    @Test
    void given10RowsAnd20Seats_whenCalculateTotalSeats_thenReturns200() {
        Showroom showroom = new Showroom("Test Hall", 10, 20);

        assertThat(showroom.getTotalSeats()).isEqualTo(200);
    }

    @Test
    void given5RowsAnd15Seats_whenCalculateTotalSeats_thenReturns75() {
        Showroom showroom = new Showroom("Small Hall", 5, 15);

        assertThat(showroom.getTotalSeats()).isEqualTo(75);
    }

    @Test
    void given1RowAnd10Seats_whenCalculateTotalSeats_thenReturns10() {
        Showroom showroom = new Showroom("Tiny Hall", 1, 10);

        assertThat(showroom.getTotalSeats()).isEqualTo(10);
    }

    @Test
    void given15RowsAnd25Seats_whenCalculateTotalSeats_thenReturns375() {
        Showroom showroom = new Showroom("Large Hall", 15, 25);

        assertThat(showroom.getTotalSeats()).isEqualTo(375);
    }

    @Test
    void whenSetId_thenIdIsUpdated() {
        Showroom showroom = new Showroom();
        showroom.setId(1L);

        assertThat(showroom.getId()).isEqualTo(1L);
    }

    @Test
    void whenSetName_thenNameIsUpdated() {
        Showroom showroom = new Showroom();
        showroom.setName("VIP Hall");

        assertThat(showroom.getName()).isEqualTo("VIP Hall");
    }

    @Test
    void whenSetRows_thenRowsIsUpdated() {
        Showroom showroom = new Showroom();
        showroom.setRows(12);

        assertThat(showroom.getRows()).isEqualTo(12);
    }

    @Test
    void whenSetSeatsPerRow_thenSeatsPerRowIsUpdated() {
        Showroom showroom = new Showroom();
        showroom.setSeatsPerRow(30);

        assertThat(showroom.getSeatsPerRow()).isEqualTo(30);
    }

    @Test
    void whenTotalSeatsIsCalculatedMultipleTimes_thenReturnsConsistentResult() {
        Showroom showroom = new Showroom("Test", 8, 12);

        int first = showroom.getTotalSeats();
        int second = showroom.getTotalSeats();

        assertThat(first).isEqualTo(second);
        assertThat(first).isEqualTo(96);
    }
}