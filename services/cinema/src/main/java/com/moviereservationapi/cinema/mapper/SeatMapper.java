package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.seat.SeatCreateDto;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatDto;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;

public class SeatMapper {

    public static SeatDetailsDtoV1 fromSeatToDetailsDtoV1(Seat seat) {
        return SeatDetailsDtoV1.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .roomName(seat.getRoom().getName())
                .build();
    }

    public static Seat fromCreateDtoToSeat(SeatCreateDto SeatCreateDto, Room room) {
        return Seat.builder()
                .seatRow(SeatCreateDto.getSeatRow())
                .seatNumber(SeatCreateDto.getSeatNumber())
                .room(room)
                .build();
    }

    public static SeatDto fromSeatToDto(Seat seat) {
        return SeatDto.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .build();
    }

}
