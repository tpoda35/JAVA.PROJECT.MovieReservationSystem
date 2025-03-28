package com.moviereservationapi.reservation.service;

import com.moviereservationapi.reservation.dto.ReservationDto;
import com.moviereservationapi.reservation.dto.ReservationManageDto;
import com.moviereservationapi.reservation.dto.ReservationResponseDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IReservationService {
    ReservationResponseDto addReservation(ReservationManageDto reservationManageDto);
    CompletableFuture<ReservationDto> getReservation(Long reservationId);
    void deleteReservation(Long reservationId);
    CompletableFuture<Page<ReservationDto>> getUserReservations(int pageNum, int pageSize, Long userId);
}
