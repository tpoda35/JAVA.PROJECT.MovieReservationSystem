package com.moviereservationapi.reservation.service;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
import com.moviereservationapi.reservation.dto.reservation.ReservationCreateDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import com.moviereservationapi.reservation.dto.reservation.ReservationResponseDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IReservationService {
    ReservationResponseDto addReservationAndCreateCheckoutUrl(ReservationCreateDto reservationCreateDto);
    CompletableFuture<ReservationDetailsDtoV1> getReservation(Long reservationId);
    void deleteReservation(Long reservationId);
    CompletableFuture<Page<ReservationDetailsDtoV2>> getLoggedInUserReservations(int pageNum, int pageSize);
}
