package com.cinema.springcinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.springcinema.domain.Screening;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long>, JpaSpecificationExecutor<Screening> {

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.showroom WHERE s.id = :id")
    Optional<Screening> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.showroom WHERE s.movie.id = :movieId AND s.startTime >= :from ORDER BY s.startTime")
    List<Screening> findUpcomingByMovieId(@Param("movieId") Long movieId, @Param("from") LocalDateTime from);

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.showroom WHERE s.startTime >= :from AND s.startTime < :to ORDER BY s.startTime")
    List<Screening> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT s FROM Screening s JOIN FETCH s.movie JOIN FETCH s.showroom WHERE s.startTime >= :from ORDER BY s.startTime")
    List<Screening> findUpcoming(@Param("from") LocalDateTime from);
}
