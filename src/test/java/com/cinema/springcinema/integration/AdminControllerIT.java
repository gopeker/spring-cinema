package com.cinema.springcinema.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AdminControllerIT extends BaseIntegrationTest {

    private static final String ADMIN_EMAIL    = "admin@cinema.com";
    private static final String ADMIN_PASSWORD = "password";

    // ─── Movies ───────────────────────────────────────────────────────────────

    @Test
    void getAdminMovies_asAdmin_returnsMovieList() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/movies")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getAdminMovies_asUser_returns403() throws Exception {
        String token = registerAndGetToken("RegUser", "reguser@it.com", "password123");

        mockMvc.perform(get("/api/admin/movies")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminMovies_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/movies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMovie_asAdmin_returnsCreatedMovie() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"title":"Test Movie","description":"A test film","duration":90,"posterUrl":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Test Movie"))
                .andExpect(jsonPath("$.duration").value(90));
    }

    @Test
    void createMovie_asUser_returns403() throws Exception {
        String token = registerAndGetToken("RegUser2", "reguser2@it.com", "password123");

        mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"title":"Forbidden Film","description":"","duration":60,"posterUrl":""}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createMovie_missingTitle_returns400() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"description":"No title here","duration":90,"posterUrl":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMovie_asAdmin_returnsUpdatedMovie() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Create first
        String created = mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"title":"Original Title","description":"","duration":100,"posterUrl":""}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        // Update
        mockMvc.perform(put("/api/admin/movies/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"title":"Updated Title","description":"Updated","duration":120,"posterUrl":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void deleteMovie_asAdmin_returns204() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        String created = mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"title":"To Be Deleted","description":"","duration":80,"posterUrl":""}
                                """))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(delete("/api/admin/movies/" + id)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/movies/" + id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Movie not found")));
    }

    // ─── Screenings ───────────────────────────────────────────────────────────

    @Test
    void getAdminScreenings_asAdmin_returnsScreeningList() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/screenings")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void createScreening_asAdmin_returnsCreatedScreening() throws Exception {
        String token = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        // Resolve a real movie and showroom id
        String moviesJson = mockMvc.perform(get("/api/movies")).andReturn().getResponse().getContentAsString();
        long movieId = objectMapper.readTree(moviesJson).get(0).get("id").asLong();

        String showroomsJson = mockMvc.perform(get("/api/showrooms")
                        .header("Authorization", token))
                .andReturn().getResponse().getContentAsString();
        long showroomId = objectMapper.readTree(showroomsJson).get(0).get("id").asLong();

        mockMvc.perform(post("/api/admin/screenings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content("""
                                {"movieId":%d,"showroomId":%d,"startTime":"2099-12-31T20:00:00","basePrice":12.50}
                                """.formatted(movieId, showroomId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.movie.id").value(movieId))
                .andExpect(jsonPath("$.basePrice").value(12.50));
    }
}
