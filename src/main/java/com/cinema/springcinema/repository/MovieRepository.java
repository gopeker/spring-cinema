package com.cinema.springcinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cinema.springcinema.domain.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
