package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDto;

import java.util.List;

public interface ISeatFeignService {
    List<SeatDto> getSeats(List<Long> seatIds);
}
