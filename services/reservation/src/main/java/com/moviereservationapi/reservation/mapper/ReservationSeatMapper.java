package com.moviereservationapi.reservation.mapper;

import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;

public class ReservationSeatMapper {

    public static ReservationSeat toReservationSeat(Long seatId, Reservation reservation) {
        return ReservationSeat.builder()
                .seatId(seatId)
                .reservation(reservation)
                .build();
    }

}
