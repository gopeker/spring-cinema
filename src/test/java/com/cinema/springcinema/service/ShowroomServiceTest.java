package com.cinema.springcinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinema.springcinema.domain.Showroom;
import com.cinema.springcinema.dto.ShowroomDto;
import com.cinema.springcinema.repository.ShowroomRepository;

@ExtendWith(MockitoExtension.class)
class ShowroomServiceTest {

    @Mock
    private ShowroomRepository showroomRepository;

    @InjectMocks
    private ShowroomService showroomService;

    private Showroom showroom;

    @BeforeEach
    void setUp() {
        showroom = new Showroom("Main Hall", 10, 20);
        showroom.setId(1L);
    }

    @Test
    void whenFindAll_thenReturnsAllShowrooms() {
        when(showroomRepository.findAll()).thenReturn(List.of(showroom));

        List<ShowroomDto> result = showroomService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Main Hall");
        assertThat(result.get(0).rows()).isEqualTo(10);
        assertThat(result.get(0).seatsPerRow()).isEqualTo(20);
    }

    @Test
    void whenFindAllWithNoShowrooms_thenReturnsEmptyList() {
        when(showroomRepository.findAll()).thenReturn(List.of());

        List<ShowroomDto> result = showroomService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void givenShowroomId_whenFindById_thenReturnsShowroom() {
        when(showroomRepository.findById(1L)).thenReturn(Optional.of(showroom));

        ShowroomDto result = showroomService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Main Hall");
        assertThat(result.totalSeats()).isEqualTo(200);
    }

    @Test
    void givenNonExistentShowroomId_whenFindById_thenThrowsException() {
        when(showroomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showroomService.findById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Showroom not found: 999");
    }

    @Test
    void whenFindAllWithMultipleShowrooms_thenReturnsAll() {
        Showroom showroom2 = new Showroom("VIP Hall", 5, 10);
        showroom2.setId(2L);

        when(showroomRepository.findAll()).thenReturn(List.of(showroom, showroom2));

        List<ShowroomDto> result = showroomService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Main Hall");
        assertThat(result.get(1).name()).isEqualTo("VIP Hall");
    }

    @Test
    void givenShowroom_whenFindById_thenCalculatesTotalSeatsCorrectly() {
        Showroom smallShowroom = new Showroom("Small Hall", 3, 8);
        smallShowroom.setId(2L);

        when(showroomRepository.findById(2L)).thenReturn(Optional.of(smallShowroom));

        ShowroomDto result = showroomService.findById(2L);

        assertThat(result.totalSeats()).isEqualTo(24);
    }
}