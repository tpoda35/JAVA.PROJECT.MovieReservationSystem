package com.moviereservationapi.reservation.service;

public interface IReservationFeignService {
    void deleteReservationWithShowtimeId(Long showtimeId);
    void changeStatusToPaid(Long reservationId);
    void changeStatusToFailed(Long reservationId);
    void deleteExpiredReservationById(Long reservationId);
}
