package com.moviereservationapi.reservation.service;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV3;

import java.util.List;

public interface IReservationSeatFeignService {
    List<Long> findReservedSeatIdsByShowtimeId(Long showtimeId);
    ReservationDetailsDtoV3 findSeatIdsAndShowtimeIdByReservationId(Long reservationId);
}
