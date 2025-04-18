package com.moviereservationapi.reservation.service;

import com.moviereservationapi.reservation.dto.reservation.ReservationPayment;

public interface IReservationFeignService {
    void deleteReservationWithShowtimeId(Long showtimeId);
    void changeStatusToPaid(Long reservationId);
    void changeStatusToFailed(Long reservationId);
    void changeStatusToUnder_Payment(Long reservationId);
    ReservationPayment getPaymentDataByReservationId(Long reservationId);
}
