package com.cinema.springcinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cinema.springcinema.domain.Showroom;

@Repository
public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
}
