package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.seat.SeatCreateDto;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatEditDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ISeatService {
    CompletableFuture<SeatDetailsDtoV1> getSeatById(Long seatId);
    CompletableFuture<List<SeatDetailsDtoV1>> getAllSeatByRoom(Long roomId);
    SeatDetailsDtoV1 addSeat(SeatCreateDto SeatCreateDto);
    SeatDetailsDtoV1 editSeat(Long seatId, SeatEditDto seatEditDto);
    void deleteSeat(Long seatId);
}
