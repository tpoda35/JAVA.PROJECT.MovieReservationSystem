package com.moviereservationapi.reservation.service;

public interface IReservationFeignService {
    void deleteReservationWithShowtimeId(Long showtimeId);
}
