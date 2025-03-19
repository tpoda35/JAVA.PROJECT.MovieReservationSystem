package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISeatService {
    CompletableFuture<SeatDto> getSeat(Long seatId);
    CompletableFuture<List<SeatDto>> getAllSeat(Long roomId);
}
