package com.cinema.springcinema.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MovieControllerIT extends BaseIntegrationTest {

    @Test
    void getMovies_public_returnsSeededMovies() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").isNotEmpty());
    }

    @Test
    void getMovies_noAuth_returns200() throws Exception {
        // Public endpoint — no Authorization header required
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());
    }

    @Test
    void getMovieById_seededMovie_returnsMovieDetails() throws Exception {
        // Get the first movie from the list, then fetch it by id
        String listJson = mockMvc.perform(get("/api/movies"))
                .andReturn().getResponse().getContentAsString();

        long firstId = objectMapper.readTree(listJson).get(0).get("id").asLong();

        mockMvc.perform(get("/api/movies/" + firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.duration").isNumber());
    }

    @Test
    void getMovieById_nonExistentId_returns400() throws Exception {
        mockMvc.perform(get("/api/movies/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Movie not found")));
    }

    @Test
    void getMovies_containsKnownSeededTitle() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("The Matrix")));
    }
}
