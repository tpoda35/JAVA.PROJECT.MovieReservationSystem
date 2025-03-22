package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.ReservationDto;
import com.moviereservationapi.reservation.dto.ReservationManageDto;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;

    @Override
    public ReservationDto addReservation(ReservationManageDto reservationManageDto) {
        return null;
    }

    @Override
    public CompletableFuture<ReservationDto> getReservation(Long reservationId) {
        return null;
    }

    @Override
    public void deleteReservation(Long reservationId) {

    }

    @Override
    public CompletableFuture<Page<ReservationDto>> getUserReservations(int pageNum, int pageSize, Long userId) {
        return null;
    }

}
