package com.cinema.springcinema.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GlobalExceptionHandler — verifies that HTTP error
 * conditions produce the correct status codes and JSON error bodies.
 */
@IntegrationTest
class GlobalExceptionHandlerIT extends BaseIntegrationTest {

    @Test
    void unknownStaticResource_returns404WithErrorBody() throws Exception {
        // /static/** is permit-all; a missing file reaches NoResourceFoundException
        mockMvc.perform(get("/static/does-not-exist.js"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString("Resource not found")));
    }

    @Test
    void malformedJson_returns400WithErrorBody() throws Exception {
        // /api/auth/** is permit-all, so malformed body reaches the exception handler
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));
    }

    @Test
    void wrongHttpMethod_returns405WithErrorBody() throws Exception {
        // POST /api/auth/login is mapped; DELETE is not — reaches the exception handler
        // because /api/auth/** is permit-all regardless of method
        mockMvc.perform(delete("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value(containsString("Method not allowed")));
    }
}
