package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV2;
import com.moviereservationapi.cinema.dto.seat.SeatDto;

import java.util.List;

public interface ISeatFeignService {
    List<SeatDetailsDtoV2> getSeats(List<Long> seatIds);
    List<SeatDto> getSeatsByRoomId(Long roomId);
}
