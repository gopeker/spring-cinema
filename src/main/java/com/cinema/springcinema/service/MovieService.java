package com.cinema.springcinema.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.dto.MovieCreateRequest;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.repository.MovieRepository;
import com.cinema.springcinema.repository.MovieSpecifications;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public java.util.List<MovieDto> findAll() {
        return movieRepository.findAll(Sort.by("id")).stream().map(this::toDto).toList();
    }

    public Page<MovieDto> findAll(Pageable pageable) {
        return movieRepository.findAll(pageable).map(this::toDto);
    }

    public MovieDto findById(Long id) {
        return movieRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));
    }

    public MovieDto create(MovieCreateRequest request) {
        Movie movie = new Movie(request.title(), request.description(), request.duration(), request.posterUrl());
        return toDto(movieRepository.save(movie));
    }

    public MovieDto update(Long id, MovieCreateRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setDuration(request.duration());
        movie.setPosterUrl(request.posterUrl());
        return toDto(movieRepository.save(movie));
    }

    public Page<MovieDto> search(String title, String description, Integer minDuration, Integer maxDuration, Pageable pageable) {
        Specification<Movie> spec = Specification
                .where(MovieSpecifications.titleContains(title))
                .and(MovieSpecifications.descriptionContains(description))
                .and(MovieSpecifications.durationBetween(minDuration, maxDuration));
        return movieRepository.findAll(spec, pageable).map(this::toDto);
    }

    public Page<MovieDto> searchByName(String name, Pageable pageable) {
        Specification<Movie> spec = MovieSpecifications.titleContains(name);
        return movieRepository.findAll(spec, pageable).map(this::toDto);
    }

    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found: " + id);
        }
        movieRepository.deleteById(id);
    }

    private MovieDto toDto(Movie movie) {
        return new MovieDto(movie.getId(), movie.getTitle(), movie.getDescription(),
                movie.getDuration(), movie.getPosterUrl());
    }
}
