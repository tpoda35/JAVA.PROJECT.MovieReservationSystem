package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.RoomDetailsDtoV2;
import com.moviereservationapi.cinema.dto.RoomManageDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IRoomService {
    CompletableFuture<Page<RoomDetailsDtoV1>> getRoomsByCinema(Long cinemaId, int pageSize, int pageNum);
    CompletableFuture<RoomDetailsDtoV2> getRoom(Long roomId);
    RoomDetailsDtoV1 addRoom(RoomManageDto roomManageDto);
    RoomDetailsDtoV2 editRoom(RoomManageDto roomManageDto, Long roomId);
    void deleteRoom(Long roomId);
}
