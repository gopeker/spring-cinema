package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.dto.MovieCreateRequest;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private MovieDto movieDto;
    private MovieCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        movie = new Movie("Inception", "A mind-bending thriller", 148, "https://example.com/poster.jpg");
        movie.setId(1L);
        movieDto = new MovieDto(1L, "Inception", "A mind-bending thriller", 148, "https://example.com/poster.jpg");
        createRequest = new MovieCreateRequest("Inception", "A mind-bending thriller", 148, "https://example.com/poster.jpg");
    }

    @Test
    void whenFindAll_thenReturnsAllMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Inception");
    }

    @Test
    void whenFindAllWithNoMovies_thenReturnsEmptyList() {
        when(movieRepository.findAll()).thenReturn(List.of());

        List<MovieDto> result = movieService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void givenMovieId_whenFindById_thenReturnsMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieDto result = movieService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Inception");
    }

    @Test
    void givenNonExistentMovieId_whenFindById_thenThrowsException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Movie not found: 999");
    }

    @Test
    void givenValidCreateRequest_whenCreate_thenReturnsMovieDto() {
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        MovieDto result = movieService.create(createRequest);

        assertThat(result.title()).isEqualTo("Inception");
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void givenValidUpdateRequest_whenUpdate_thenReturnsUpdatedMovie() {
        MovieCreateRequest updateRequest = new MovieCreateRequest("Inception Updated", "Updated description", 150, "new-poster.jpg");
        Movie updatedMovie = new Movie("Inception Updated", "Updated description", 150, "new-poster.jpg");
        updatedMovie.setId(1L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

        MovieDto result = movieService.update(1L, updateRequest);

        assertThat(result.title()).isEqualTo("Inception Updated");
        assertThat(result.description()).isEqualTo("Updated description");
    }

    @Test
    void givenNonExistentMovieId_whenUpdate_thenThrowsException() {
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.update(999L, createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Movie not found: 999");
    }

    @Test
    void givenExistingMovieId_whenDelete_thenDeletesMovie() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        doNothing().when(movieRepository).deleteById(1L);

        movieService.delete(1L);

        verify(movieRepository).deleteById(1L);
    }

    @Test
    void givenNonExistentMovieId_whenDelete_thenThrowsException() {
        when(movieRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> movieService.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Movie not found: 999");
    }

    @Test
    void whenFindAllWithMultipleMovies_thenReturnsAll() {
        Movie movie2 = new Movie("The Matrix", "Sci-fi classic", 136, "matrix-poster.jpg");
        movie2.setId(2L);

        when(movieRepository.findAll()).thenReturn(List.of(movie, movie2));

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(2);
    }
}