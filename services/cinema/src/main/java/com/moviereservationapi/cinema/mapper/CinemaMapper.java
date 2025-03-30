package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.model.Cinema;

public class CinemaMapper {

    public static CinemaDetailsDtoV1 fromCinemaToDetailsDto(Cinema cinema) {
        return CinemaDetailsDtoV1.builder()
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

    public static CinemaDetailsDtoV2 fromCinemaToCinemaDto(Cinema cinema) {
        return CinemaDetailsDtoV2.builder()
                .name(cinema.getName())
                .location(cinema.getLocation())
                .build();
    }

}
