package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.SeatManageDto;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;

public class SeatMapper {

    public static SeatDetailsDtoV1 fromSeatToDto(Seat seat) {
        return SeatDetailsDtoV1.builder()
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
