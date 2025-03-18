package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.model.Seat;

public class SeatMapper {

    public static SeatDto fromSeatToDto(Seat seat) {
        return SeatDto.builder()
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .room(seat.getRoom())
                .build();
    }

}
