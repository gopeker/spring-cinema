package com.cinema.springcinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.springcinema.domain.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.screening s JOIN FETCH s.movie JOIN FETCH s.showroom JOIN FETCH t.user WHERE t.user.id = :userId ORDER BY t.purchaseTime DESC")
    List<Ticket> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.user JOIN FETCH t.screening s JOIN FETCH s.movie JOIN FETCH s.showroom WHERE t.id = :id")
    Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

    boolean existsByScreeningIdAndSeatRowAndSeatNumber(Long screeningId, String seatRow, Integer seatNumber);

    List<Ticket> findByScreeningIdAndStatus(Long screeningId, Ticket.Status status);

    @Query("SELECT t FROM Ticket t WHERE t.screening.id = :screeningId AND t.seatRow = :seatRow AND t.seatNumber = :seatNumber AND t.status = 'CONFIRMED'")
    Optional<Ticket> findConfirmedTicket(@Param("screeningId") Long screeningId, @Param("seatRow") String seatRow, @Param("seatNumber") Integer seatNumber);
}
