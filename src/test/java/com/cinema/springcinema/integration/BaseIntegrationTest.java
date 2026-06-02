package com.cinema.springcinema.integration;

import com.cinema.springcinema.dto.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base class for all integration tests.
 *
 * @IntegrationTest bundles @SpringBootTest, @Import(TestcontainersConfiguration)
 * and @DirtiesContext as a single composed annotation. Applying it here — rather
 * than only on the base class — means IntelliJ resolves the full Spring context
 * when a subclass is run directly, without relying on annotation inheritance.
 */
@IntegrationTest
public abstract class BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /** Register a new user and return its Bearer token. */
    protected String registerAndGetToken(String name, String email, String password) throws Exception {
        String body = """
                {"name":"%s","email":"%s","password":"%s"}
                """.formatted(name, email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return "Bearer " + auth.token();
    }

    /** Login an existing user and return its Bearer token. */
    protected String loginAndGetToken(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();

        AuthResponse auth = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return "Bearer " + auth.token();
    }
}
