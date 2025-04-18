package com.moviereservationapi.reservation.service;

import java.util.List;

public interface IReservationSeatFeignService {
    List<Long> findReservedSeatIdsByShowtimeId(Long showtimeId);
}
