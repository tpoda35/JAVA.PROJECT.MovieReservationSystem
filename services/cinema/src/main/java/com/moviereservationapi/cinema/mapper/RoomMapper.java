package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.model.Room;

public class RoomMapper {

    public static RoomDetailsDtoV1 fromRoomToDetailsDtoV1(Room room) {
        return RoomDetailsDtoV1.builder()
                .id(room.getId())
                .name(room.getName())
                .totalSeat(room.getTotalSeat())
                .build();
    }

}
