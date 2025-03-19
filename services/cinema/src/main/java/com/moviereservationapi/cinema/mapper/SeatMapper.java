package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatManageDto;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;

public class SeatMapper {

    public static SeatDto fromSeatToDto(Seat seat) {
        return SeatDto.builder()
                .id(seat.getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .roomName(seat.getRoom().getName())
                .build();
    }

    public static Seat fromManageDtoToSeat(SeatManageDto seatManageDto, Room room) {
        return Seat.builder()
                .seatRow(seatManageDto.getSeatRow())
                .seatNumber(seatManageDto.getSeatNumber())
                .room(room)
                .build();
    }

}
