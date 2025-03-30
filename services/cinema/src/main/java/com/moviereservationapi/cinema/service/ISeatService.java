package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.SeatManageDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISeatService {
    CompletableFuture<SeatDetailsDtoV1> getSeat(Long seatId);
    CompletableFuture<List<SeatDetailsDtoV1>> getAllSeatByRoom(Long roomId);
    SeatDetailsDtoV1 addSeat(SeatManageDto seatManageDto);
    SeatDetailsDtoV1 editSeat(Long seatId, SeatManageDto seatManageDto);
    void deleteSeat(Long seatId);
}
