package com.cinema.springcinema.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.dto.ShowroomDto;
import com.cinema.springcinema.repository.ShowroomRepository;

@Service
public class ShowroomService {

    private final ShowroomRepository showroomRepository;

    public ShowroomService(ShowroomRepository showroomRepository) {
        this.showroomRepository = showroomRepository;
    }

    public List<ShowroomDto> findAll() {
        return showroomRepository.findAll().stream().map(this::toDto).toList();
    }

    public ShowroomDto findById(Long id) {
        return showroomRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Showroom not found: " + id));
    }

    private ShowroomDto toDto(Showroom showroom) {
        return new ShowroomDto(showroom.getId(), showroom.getName(), showroom.getRows(),
                showroom.getSeatsPerRow(), showroom.getTotalSeats());
    }
}
