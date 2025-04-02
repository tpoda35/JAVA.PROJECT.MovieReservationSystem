package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV2;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IRoomService {
    CompletableFuture<Page<RoomDetailsDtoV1>> getRoomsByCinema(Long cinemaId, int pageSize, int pageNum);
    CompletableFuture<RoomDetailsDtoV1> getRoom(Long roomId);
    RoomDetailsDtoV1 addRoom(RoomManageDtoV1 roomManageDtoV1);
    RoomDetailsDtoV1 editRoom(RoomManageDtoV2 roomManageDtoV2, Long roomId);
    void deleteRoom(Long roomId);
}
