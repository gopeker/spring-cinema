package com.cinema.springcinema.integration;

import com.cinema.springcinema.SpringCinemaApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.lang.annotation.*;

/**
 * Composed annotation for integration tests.
 *
 * Explicitly references SpringCinemaApplication so IntelliJ can resolve
 * the full Spring Boot auto-configuration context (including ObjectMapper,
 * Security, JPA etc.) when a single test class is run directly.
 *
 * The PostgreSQL Testcontainer is declared as a static @ServiceConnection
 * field on BaseIntegrationTest, which is the Spring Boot 4-idiomatic way
 * to wire Testcontainers without @Import(TestcontainersConfiguration).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(
        classes = SpringCinemaApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public @interface IntegrationTest {
}
