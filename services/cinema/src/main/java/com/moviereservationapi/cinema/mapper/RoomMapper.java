package com.moviereservationapi.cinema.mapper;

import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV1;
import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.model.Room;

public class RoomMapper {

    public static RoomDetailsDtoV1 fromRoomToDetailsDtoV1(Room room) {
        return RoomDetailsDtoV1.builder()
                .id(room.getId())
                .name(room.getName())
                .totalSeat(room.getTotalSeat())
                .build();
    }

    public static Room fromManageDtoV1ToRoom(RoomManageDtoV1 roomManageDtoV1, Cinema cinema) {
        return Room.builder()
                .name(roomManageDtoV1.getName())
                .totalSeat(roomManageDtoV1.getTotalSeat())
                .cinema(cinema)
                .build();
    }
}
