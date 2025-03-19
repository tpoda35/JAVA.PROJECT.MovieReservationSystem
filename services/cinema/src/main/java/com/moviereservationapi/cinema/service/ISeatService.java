package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatManageDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISeatService {
    CompletableFuture<SeatDto> getSeat(Long seatId);
    CompletableFuture<List<SeatDto>> getAllSeatByRoom(Long roomId);
    SeatDto addSeat(SeatManageDto seatManageDto);
}
