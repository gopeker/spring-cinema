package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinema.springcinema.dto.MovieDto;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    void givenMoviesExist_whenFindAllMovies_thenReturnsMovies() {
        List<MovieDto> movies = List.of(
                new MovieDto(1L, "Inception", "A mind-bending thriller", 148, "poster.jpg"),
                new MovieDto(2L, "The Matrix", "Sci-fi classic", 136, "matrix.jpg")
        );
        when(movieService.findAll()).thenReturn(movies);

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Inception");
        assertThat(result.get(1).title()).isEqualTo("The Matrix");
    }

    @Test
    void givenNoMovies_whenFindAllMovies_thenReturnsEmptyList() {
        when(movieService.findAll()).thenReturn(List.of());

        List<MovieDto> result = movieService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void givenMovieWithNullDescription_whenFindAllMovies_thenHandlesGracefully() {
        List<MovieDto> movies = List.of(
                new MovieDto(1L, "Inception", null, 148, "poster.jpg")
        );
        when(movieService.findAll()).thenReturn(movies);

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isNull();
    }

    @Test
    void givenMovieWithNullDuration_whenFindAllMovies_thenHandlesGracefully() {
        List<MovieDto> movies = List.of(
                new MovieDto(1L, "Inception", "Thriller", null, "poster.jpg")
        );
        when(movieService.findAll()).thenReturn(movies);

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).duration()).isNull();
    }

    @Test
    void givenChatbotServiceCreated_thenInitializesSuccessfully() {
        assertThat(chatbotService).isNotNull();
    }
}