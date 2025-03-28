package com.moviereservationapi.reservation.mapper;

import com.moviereservationapi.reservation.dto.ReservationResponseDto;
import com.moviereservationapi.reservation.dto.SeatDto;
import com.moviereservationapi.reservation.dto.ShowtimeDto;
import com.moviereservationapi.reservation.model.Reservation;

import java.util.List;

public class ReservationMapper {

    public static ReservationResponseDto toReservationResponseDto(
            Reservation reservation,
            ShowtimeDto showtimeDto,
            List<SeatDto> seatDtos
    ) {
        return ReservationResponseDto.builder()
                .id(reservation.getId())
                .reservationTime(reservation.getReservationTime())
                .paymentStatus(reservation.getPaymentStatus())
                .seatDtos(seatDtos)
                .showtimeDto(showtimeDto)
                .build();
    }

}
