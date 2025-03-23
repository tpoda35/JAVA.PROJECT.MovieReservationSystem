package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.ReservationDto;
import com.moviereservationapi.reservation.dto.ReservationManageDto;
import com.moviereservationapi.reservation.exception.UserNotFoundException;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.repository.UserRepository;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Override
    public ReservationDto addReservation(ReservationManageDto reservationManageDto) {
        Long userId = reservationManageDto.getUserId();
        Long showtimeId = reservationManageDto.getShowtimeId();
        List<Long> seatIds = reservationManageDto.getSeatIds();

        Reservation reservation = new Reservation();
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

    private boolean checkIfExists(Long userId, Long showtimeId, List<Long> seatIds) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found.");
        }

        return false;
    }

}
