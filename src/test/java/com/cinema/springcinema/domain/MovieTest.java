package com.cinema.springcinema.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MovieTest {

    @Test
    void givenValidParameters_whenCreateMovie_thenMovieIsCreated() {
        Movie movie = new Movie("Inception", "A mind-bending thriller", 148, "https://example.com/poster.jpg");

        assertThat(movie.getTitle()).isEqualTo("Inception");
        assertThat(movie.getDescription()).isEqualTo("A mind-bending thriller");
        assertThat(movie.getDuration()).isEqualTo(148);
        assertThat(movie.getPosterUrl()).isEqualTo("https://example.com/poster.jpg");
        assertThat(movie.getId()).isNull();
    }

    @Test
    void givenNullDescription_whenCreateMovie_thenMovieIsCreatedWithNullDescription() {
        Movie movie = new Movie("Test Movie", null, 120, null);

        assertThat(movie.getTitle()).isEqualTo("Test Movie");
        assertThat(movie.getDescription()).isNull();
        assertThat(movie.getDuration()).isEqualTo(120);
        assertThat(movie.getPosterUrl()).isNull();
    }

    @Test
    void whenSetId_thenIdIsUpdated() {
        Movie movie = new Movie();
        movie.setId(1L);

        assertThat(movie.getId()).isEqualTo(1L);
    }

    @Test
    void whenSetTitle_thenTitleIsUpdated() {
        Movie movie = new Movie();
        movie.setTitle("New Title");

        assertThat(movie.getTitle()).isEqualTo("New Title");
    }

    @Test
    void whenSetDescription_thenDescriptionIsUpdated() {
        Movie movie = new Movie();
        movie.setDescription("New Description");

        assertThat(movie.getDescription()).isEqualTo("New Description");
    }

    @Test
    void whenSetDuration_thenDurationIsUpdated() {
        Movie movie = new Movie();
        movie.setDuration(180);

        assertThat(movie.getDuration()).isEqualTo(180);
    }

    @Test
    void whenSetPosterUrl_thenPosterUrlIsUpdated() {
        Movie movie = new Movie();
        movie.setPosterUrl("https://newposter.com/image.jpg");

        assertThat(movie.getPosterUrl()).isEqualTo("https://newposter.com/image.jpg");
    }

    @Test
    void givenLongDuration_whenCreateMovie_thenDurationIsStored() {
        Movie movie = new Movie("Long Movie", "A very long movie", 240, null);

        assertThat(movie.getDuration()).isEqualTo(240);
    }

    @Test
    void givenShortDuration_whenCreateMovie_thenDurationIsStored() {
        Movie movie = new Movie("Short Movie", "A short movie", 60, null);

        assertThat(movie.getDuration()).isEqualTo(60);
    }
}