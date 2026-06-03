package com.cinema.springcinema.repository;

import org.springframework.data.jpa.domain.Specification;

import com.cinema.springcinema.domain.Movie;

public class MovieSpecifications {

    private MovieSpecifications() {}

    public static Specification<Movie> titleContains(String title) {
        return (root, query, cb) -> title == null || title.isBlank() ? null
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Movie> descriptionContains(String description) {
        return (root, query, cb) -> description == null || description.isBlank() ? null
                : cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Movie> durationBetween(Integer minDuration, Integer maxDuration) {
        return (root, query, cb) -> {
            if (minDuration != null && maxDuration != null) {
                return cb.between(root.get("duration"), minDuration, maxDuration);
            } else if (minDuration != null) {
                return cb.greaterThanOrEqualTo(root.get("duration"), minDuration);
            } else if (maxDuration != null) {
                return cb.lessThanOrEqualTo(root.get("duration"), maxDuration);
            }
            return null;
        };
    }
}
