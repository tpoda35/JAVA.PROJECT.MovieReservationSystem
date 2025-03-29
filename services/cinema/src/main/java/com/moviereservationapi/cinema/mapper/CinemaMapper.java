package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.CinemaDetailsDto;
import com.moviereservationapi.cinema.dto.CinemaDto;
import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.model.Cinema;

public class CinemaMapper {

    public static CinemaDetailsDto fromCinemaToDetailsDto(Cinema cinema) {
        return CinemaDetailsDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .location(cinema.getLocation())
                .roomNum(cinema.getRoomNum())
                .build();
    }

    public static Cinema fromCinemaManageDtoToCinema(CinemaManageDto cinemaManageDto) {
        return Cinema.builder()
                .name(cinemaManageDto.getName())
                .location(cinemaManageDto.getLocation())
                .build();
    }

    public static CinemaDto fromCinemaToCinemaDto(Cinema cinema) {
        return CinemaDto.builder()
                .name(cinema.getName())
                .location(cinema.getLocation())
                .build();
    }

}
