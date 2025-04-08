package com.moviereservationapi.reservation.mapper;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import com.moviereservationapi.reservation.dto.reservation.ReservationResponseDto;
import com.moviereservationapi.reservation.dto.feign.SeatDto;
import com.moviereservationapi.reservation.dto.feign.ShowtimeDto;
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

    public static ReservationDetailsDtoV1 fromReservationToDetailsDtoV1(
            Reservation reservation,
            List<SeatDto> seatDtos
    ) {
        return ReservationDetailsDtoV1.builder()
                .reservationTime(reservation.getReservationTime())
                .paymentStatus(reservation.getPaymentStatus())
                .seatDtos(seatDtos)
                .build();
    }

    public static ReservationDetailsDtoV2 fromReservationToDetailsDtoV2(
            Reservation reservation
    ) {
        return ReservationDetailsDtoV2.builder()
                .id(reservation.getId())
                .reservationTime(reservation.getReservationTime())
                .paymentStatus(reservation.getPaymentStatus())
                .build();
    }

}
