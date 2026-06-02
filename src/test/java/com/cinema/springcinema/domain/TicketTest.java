package com.cinema.springcinema.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void givenValidParameters_whenCreateTicket_thenTicketIsCreated() {
        Movie movie = new Movie("Movie", "Desc", 120, null);
        Showroom showroom = new Showroom("Hall 1", 10, 20);
        Screening screening = new Screening(movie, showroom, LocalDateTime.now().plusDays(1), new BigDecimal("15.00"));
        User user = new User("John", "john@example.com", "pass", User.Role.USER);

        Ticket ticket = new Ticket(screening, user, "A", 5, new BigDecimal("18.00"));

        assertThat(ticket.getScreening()).isEqualTo(screening);
        assertThat(ticket.getUser()).isEqualTo(user);
        assertThat(ticket.getSeatRow()).isEqualTo("A");
        assertThat(ticket.getSeatNumber()).isEqualTo(5);
        assertThat(ticket.getPrice()).isEqualByComparingTo(new BigDecimal("18.00"));
        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CONFIRMED);
        assertThat(ticket.getId()).isNull();
    }

    @Test
    void whenSetId_thenIdIsUpdated() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        assertThat(ticket.getId()).isEqualTo(1L);
    }

    @Test
    void whenSetScreening_thenScreeningIsUpdated() {
        Ticket ticket = new Ticket();
        Movie movie = new Movie("Movie", null, 100, null);
        Showroom showroom = new Showroom("Hall", 5, 10);
        Screening screening = new Screening(movie, showroom, LocalDateTime.now().plusDays(1), new BigDecimal("10.00"));
        ticket.setScreening(screening);

        assertThat(ticket.getScreening()).isEqualTo(screening);
    }

    @Test
    void whenSetUser_thenUserIsUpdated() {
        Ticket ticket = new Ticket();
        User user = new User("Jane", "jane@example.com", "pass", User.Role.USER);
        ticket.setUser(user);

        assertThat(ticket.getUser()).isEqualTo(user);
    }

    @Test
    void whenSetSeatRow_thenSeatRowIsUpdated() {
        Ticket ticket = new Ticket();
        ticket.setSeatRow("C");

        assertThat(ticket.getSeatRow()).isEqualTo("C");
    }

    @Test
    void whenSetSeatNumber_thenSeatNumberIsUpdated() {
        Ticket ticket = new Ticket();
        ticket.setSeatNumber(15);

        assertThat(ticket.getSeatNumber()).isEqualTo(15);
    }

    @Test
    void whenSetPrice_thenPriceIsUpdated() {
        Ticket ticket = new Ticket();
        ticket.setPrice(new BigDecimal("25.00"));

        assertThat(ticket.getPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    void whenSetStatusToCancelled_thenStatusIsCancelled() {
        Ticket ticket = new Ticket();
        ticket.setStatus(Ticket.Status.CANCELLED);

        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CANCELLED);
    }

    @Test
    void whenSetStatusToConfirmed_thenStatusIsConfirmed() {
        Ticket ticket = new Ticket();
        ticket.setStatus(Ticket.Status.CONFIRMED);

        assertThat(ticket.getStatus()).isEqualTo(Ticket.Status.CONFIRMED);
    }

    @Test
    void whenSetPurchaseTime_thenPurchaseTimeIsUpdated() {
        Ticket ticket = new Ticket();
        LocalDateTime purchaseTime = LocalDateTime.now();
        ticket.setPurchaseTime(purchaseTime);

        assertThat(ticket.getPurchaseTime()).isEqualTo(purchaseTime);
    }

    @Test
    void givenPremiumPricing_whenCreateTicket_thenPriceReflectsTier() {
        Movie movie = new Movie("Movie", "Desc", 120, null);
        Showroom showroom = new Showroom("Hall 1", 10, 20);
        Screening screening = new Screening(movie, showroom, LocalDateTime.now().plusDays(1), new BigDecimal("15.00"));
        User user = new User("John", "john@example.com", "pass", User.Role.USER);

        Ticket premiumTicket = new Ticket(screening, user, "A", 1, new BigDecimal("18.00"));
        Ticket standardTicket = new Ticket(screening, user, "E", 5, new BigDecimal("15.00"));
        Ticket economyTicket = new Ticket(screening, user, "J", 10, new BigDecimal("13.00"));

        assertThat(premiumTicket.getPrice()).isEqualByComparingTo("18.00");
        assertThat(standardTicket.getPrice()).isEqualByComparingTo("15.00");
        assertThat(economyTicket.getPrice()).isEqualByComparingTo("13.00");
    }
}