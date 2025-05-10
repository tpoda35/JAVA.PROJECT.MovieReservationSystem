package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.exception.ReservationNotFoundException;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.moviereservationapi.reservation.Enum.PaymentStatus.FAILED;
import static com.moviereservationapi.reservation.Enum.PaymentStatus.PAID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationFeignService implements IReservationFeignService {

    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void deleteReservationWithShowtimeId(Long showtimeId) {
        reservationRepository.removeAllByShowtimeId(showtimeId);
    }

    @Override
    @Transactional
    public void changeStatusToPaid(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setPaymentStatus(PAID);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void changeStatusToFailed(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setPaymentStatus(FAILED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void deleteExpiredReservationById(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Reservation with the id of {} not found.", reservationId);
                    return new ReservationNotFoundException("Reservation not found.");
                });
    }

}
