package com.moviereservationapi.movie.mapper;

import com.moviereservationapi.movie.dto.CinemaDto;
import com.moviereservationapi.movie.dto.CinemaManageDto;
import com.moviereservationapi.movie.model.Cinema;

public class CinemaMapper {

    public static CinemaDto fromCinemaToDto(Cinema cinema) {
        return CinemaDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .build();
    }

    public static Cinema fromManageDtoToCinema(CinemaManageDto cinemaManageDto) {
        return Cinema.builder()
                .name(cinemaManageDto.getName())
                .address(cinemaManageDto.getAddress())
                .build();
    }

}
