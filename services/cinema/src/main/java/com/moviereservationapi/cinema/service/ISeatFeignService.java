package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDetailsDtoV1;

import java.util.List;

public interface ISeatFeignService {
    List<SeatDetailsDtoV1> getSeats(List<Long> seatIds);
}
