package com.cinema.springcinema.integration;

import com.cinema.springcinema.integration.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
class TicketControllerIT extends BaseIntegrationTest {

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Returns [screeningId, seatRow, seatNumber] for a free seat in a future screening. */
    private long[] resolveFreeSeat() throws Exception {
        String screeningsJson = mockMvc.perform(get("/api/screenings"))
                .andReturn().getResponse().getContentAsString();
        long screeningId = objectMapper.readTree(screeningsJson).get(0).get("id").asLong();

        String seatsJson = mockMvc.perform(get("/api/screenings/" + screeningId + "/seats"))
                .andReturn().getResponse().getContentAsString();

        var seats = objectMapper.readTree(seatsJson);
        for (var seat : seats) {
            if (seat.get("available").asBoolean()) {
                return new long[]{
                        screeningId,
                        seat.get("seatRow").asText().chars().findFirst().orElse('A'),
                        seat.get("seatNumber").asLong()
                };
            }
        }
        throw new IllegalStateException("No available seat found in test data");
    }

    // ─── GET /api/tickets/me ──────────────────────────────────────────────────

    @Test
    void getMyTickets_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/tickets/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyTickets_authenticated_returnsEmptyListInitially() throws Exception {
        String token = registerAndGetToken("TicketUser1", "ticketuser1@it.com", "password123");

        mockMvc.perform(get("/api/tickets/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ─── POST /api/tickets ────────────────────────────────────────────────────

    @Test
    void purchaseTicket_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"screeningId":1,"seatRow":"A","seatNumber":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void purchaseTicket_validSeat_returnsTicket() throws Exception {
        String token = registerAndGetToken("TicketUser2", "ticketuser2@it.com", "password123");
        long[] seat = resolveFreeSeat();

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"screeningId":%d,"seatRow":"%s","seatNumber":%d}
                                """.formatted(seat[0], (char) seat[1], seat[2])))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.seatRow").isNotEmpty())
                .andExpect(jsonPath("$.seatNumber").isNumber())
                .andExpect(jsonPath("$.price").isNumber())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void purchaseTicket_sameSetTwice_returns400() throws Exception {
        String token1 = registerAndGetToken("TicketUser3", "ticketuser3@it.com", "password123");
        String token2 = registerAndGetToken("TicketUser4", "ticketuser4@it.com", "password123");
        long[] seat = resolveFreeSeat();

        String body = """
                {"screeningId":%d,"seatRow":"%s","seatNumber":%d}
                """.formatted(seat[0], (char) seat[1], seat[2]);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token1)
                .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token2)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Seat already taken"));
    }

    @Test
    void purchaseTicket_appearsInMyTickets() throws Exception {
        String token = registerAndGetToken("TicketUser5", "ticketuser5@it.com", "password123");
        long[] seat = resolveFreeSeat();

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content("""
                        {"screeningId":%d,"seatRow":"%s","seatNumber":%d}
                        """.formatted(seat[0], (char) seat[1], seat[2])));

        mockMvc.perform(get("/api/tickets/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    // ─── DELETE /api/tickets/{id} ─────────────────────────────────────────────

    @Test
    void cancelTicket_ownTicket_returns204() throws Exception {
        String token = registerAndGetToken("TicketUser6", "ticketuser6@it.com", "password123");
        long[] seat = resolveFreeSeat();

        String purchaseResponse = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"screeningId":%d,"seatRow":"%s","seatNumber":%d}
                                """.formatted(seat[0], (char) seat[1], seat[2])))
                .andReturn().getResponse().getContentAsString();

        long ticketId = objectMapper.readTree(purchaseResponse).get("id").asLong();

        mockMvc.perform(delete("/api/tickets/" + ticketId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelTicket_anotherUsersTicket_returns403() throws Exception {
        String owner  = registerAndGetToken("TicketUser7", "ticketuser7@it.com", "password123");
        String other  = registerAndGetToken("TicketUser8", "ticketuser8@it.com", "password123");
        long[] seat   = resolveFreeSeat();

        String purchaseResponse = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", owner)
                        .content("""
                                {"screeningId":%d,"seatRow":"%s","seatNumber":%d}
                                """.formatted(seat[0], (char) seat[1], seat[2])))
                .andReturn().getResponse().getContentAsString();

        long ticketId = objectMapper.readTree(purchaseResponse).get("id").asLong();

        mockMvc.perform(delete("/api/tickets/" + ticketId)
                        .header("Authorization", other))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelTicket_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isForbidden());
    }
}
