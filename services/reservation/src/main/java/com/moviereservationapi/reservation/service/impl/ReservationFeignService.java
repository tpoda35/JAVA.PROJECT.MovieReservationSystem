package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.reservation.ReservationPayment;
import com.moviereservationapi.reservation.exception.ReservationNotFoundException;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.model.ReservationSeat;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.moviereservationapi.reservation.Enum.PaymentStatus.*;

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
    public void changeStatusToUnder_Payment(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        reservation.setPaymentStatus(UNDER_PAYMENT);
        reservationRepository.save(reservation);
    }

    // Mapper can be used, but problems came up with the lazy loaded data.
    @Override
    @Transactional
    public ReservationPayment getPaymentDataByReservationId(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);

        return ReservationPayment.builder()
                .seatIds(reservation.getReservationSeats().stream().map(ReservationSeat::getSeatId).toList())
                .showtimeId(reservation.getShowtimeId())
                .userId(reservation.getUser().getId())
                .build();
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Reservation with the id of {} not found.", reservationId);
                    return new ReservationNotFoundException("Reservation not found.");
                });
    }

}
