package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

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
        when(movieRepository.findAll(any(Sort.class))).thenReturn(List.of(movie));

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Inception");
    }

    @Test
    void whenFindAllWithNoMovies_thenReturnsEmptyList() {
        when(movieRepository.findAll(any(Sort.class))).thenReturn(List.of());

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

        when(movieRepository.findAll(any(Sort.class))).thenReturn(List.of(movie, movie2));

        List<MovieDto> result = movieService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void givenPageable_whenFindAllPageable_thenReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        Page<MovieDto> result = movieService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Inception");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void givenEmptyPageable_whenFindAllPageable_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(), pageable, 0);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        Page<MovieDto> result = movieService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenSecondPage_whenFindAllPageable_thenReturnsCorrectPage() {
        Movie movie2 = new Movie("The Matrix", "Sci-fi", 136, "poster.jpg");
        movie2.setId(2L);
        Pageable pageable = PageRequest.of(1, 1);
        Page<Movie> page = new PageImpl<>(List.of(movie2), pageable, 2);
        when(movieRepository.findAll(pageable)).thenReturn(page);

        Page<MovieDto> result = movieService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("The Matrix");
        assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
    }

    @Test
    void givenTitleAndDuration_whenSearch_thenReturnsMatchingMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.search("Inception", null, 100, 200, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Inception");
    }

    @Test
    void givenNoSearchResults_whenSearch_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(), pageable, 0);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.search("NonExistent", null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void givenMinDurationOnly_whenSearch_thenReturnsMatchingMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.search(null, null, 120, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenAllNullFilters_whenSearch_thenReturnsAllMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.search(null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void givenSearchByName_whenSearchByName_thenReturnsMatchingMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.searchByName("Inception", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Inception");
    }

    @Test
    void givenSearchByNameNoMatch_whenSearchByName_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> page = new PageImpl<>(List.of(), pageable, 0);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.searchByName("NonExistent", pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void givenSearchByNameWithPaging_whenSearchByName_thenReturnsCorrectPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Movie> page = new PageImpl<>(List.of(movie), pageable, 1);
        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<MovieDto> result = movieService.searchByName("Inception", pageable);

        assertThat(result.getPageable().getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}