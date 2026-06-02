package com.cinema.springcinema.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class ScreeningControllerIT extends BaseIntegrationTest {

    @Test
    void getScreenings_public_returnsUpcomingScreenings() throws Exception {
        mockMvc.perform(get("/api/screenings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getScreenings_hasExpectedShape() throws Exception {
        mockMvc.perform(get("/api/screenings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].movie.title").isNotEmpty())
                .andExpect(jsonPath("$[0].showroom.name").isNotEmpty())
                .andExpect(jsonPath("$[0].startTime").isNotEmpty())
                .andExpect(jsonPath("$[0].basePrice").isNumber());
    }

    @Test
    void getScreeningsByMovieId_validMovieId_returnsFilteredResults() throws Exception {
        // Resolve a real movie id from the seeded data
        String moviesJson = mockMvc.perform(get("/api/movies"))
                .andReturn().getResponse().getContentAsString();
        long movieId = objectMapper.readTree(moviesJson).get(0).get("id").asLong();

        mockMvc.perform(get("/api/screenings?movieId=" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].movie.id", everyItem(is((int) movieId))));
    }

    @Test
    void getScreeningById_validId_returnsScreening() throws Exception {
        String json = mockMvc.perform(get("/api/screenings"))
                .andReturn().getResponse().getContentAsString();
        long screeningId = objectMapper.readTree(json).get(0).get("id").asLong();

        mockMvc.perform(get("/api/screenings/" + screeningId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(screeningId))
                .andExpect(jsonPath("$.movie").isNotEmpty())
                .andExpect(jsonPath("$.showroom").isNotEmpty());
    }

    @Test
    void getScreeningById_nonExistentId_returns400() throws Exception {
        mockMvc.perform(get("/api/screenings/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Screening not found")));
    }

    @Test
    void getSeats_validScreeningId_returnsSeatList() throws Exception {
        String json = mockMvc.perform(get("/api/screenings"))
                .andReturn().getResponse().getContentAsString();
        long screeningId = objectMapper.readTree(json).get(0).get("id").asLong();

        mockMvc.perform(get("/api/screenings/" + screeningId + "/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)))
                .andExpect(jsonPath("$[0].seatRow").isNotEmpty())
                .andExpect(jsonPath("$[0].seatNumber").isNumber())
                .andExpect(jsonPath("$[0].available").isBoolean())
                .andExpect(jsonPath("$[0].price").isNumber())
                .andExpect(jsonPath("$[0].tier").isNotEmpty());
    }

    @Test
    void getSeats_tiersAreCorrect() throws Exception {
        String json = mockMvc.perform(get("/api/screenings"))
                .andReturn().getResponse().getContentAsString();
        long screeningId = objectMapper.readTree(json).get(0).get("id").asLong();

        mockMvc.perform(get("/api/screenings/" + screeningId + "/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tier", hasItems("PREMIUM", "STANDARD", "ECONOMY")));
    }
}
