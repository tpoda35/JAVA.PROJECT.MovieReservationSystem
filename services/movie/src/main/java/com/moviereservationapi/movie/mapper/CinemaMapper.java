package com.moviereservationapi.movie.mapper;

import com.moviereservationapi.movie.dto.CinemaDto;
import com.moviereservationapi.movie.model.Cinema;

public class CinemaMapper {

    public static CinemaDto fromCinemaToDto(Cinema cinema) {
        return CinemaDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .address(cinema.getAddress())
                .build();
    }

}
