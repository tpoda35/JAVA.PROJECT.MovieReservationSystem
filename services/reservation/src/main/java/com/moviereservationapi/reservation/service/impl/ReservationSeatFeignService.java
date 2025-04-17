package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.exception.ReservationNotFoundException;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.repository.ReservationSeatRepository;
import com.moviereservationapi.reservation.service.IReservationSeatFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationSeatFeignService implements IReservationSeatFeignService {

    private final ReservationSeatRepository reservationSeatRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<Long> findReservedSeatIdsByShowtimeId(Long showtimeId) {
        return reservationSeatRepository.findReservedSeatIdsByShowtimeId(showtimeId);
    }

    @Override
    @Transactional
    public List<Long> findSeatIdsByReservationId(Long reservationId) {
        return findReservationById(reservationId).getReservationSeats()
                .stream()
                .map(ReservationSeat::getSeatId)
                .toList();
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Reservation with the id of {} not found.", reservationId);
                    return new ReservationNotFoundException("Reservation not found.");
                });
    }
}
