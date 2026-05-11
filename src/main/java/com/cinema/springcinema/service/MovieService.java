package com.cinema.springcinema.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cinema.springcinema.domain.Movie;
import com.cinema.springcinema.dto.MovieCreateRequest;
import com.cinema.springcinema.dto.MovieDto;
import com.cinema.springcinema.repository.MovieRepository;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<MovieDto> findAll() {
        return movieRepository.findAll().stream().map(this::toDto).toList();
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
