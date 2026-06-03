package com.cinema.springcinema.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.cinema.springcinema.domain.Screening;

public class ScreeningSpecifications {

    private ScreeningSpecifications() {}

    public static Specification<Screening> movieIdEquals(Long movieId) {
        return (root, query, cb) -> movieId == null ? null
                : cb.equal(root.get("movie").get("id"), movieId);
    }

    public static Specification<Screening> startTimeAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("startTime"), from);
    }

    public static Specification<Screening> startTimeBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("startTime"), to);
    }

    public static Specification<Screening> basePriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice != null && maxPrice != null) {
                return cb.between(root.get("basePrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return cb.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
            } else if (maxPrice != null) {
                return cb.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
            }
            return null;
        };
    }
}
