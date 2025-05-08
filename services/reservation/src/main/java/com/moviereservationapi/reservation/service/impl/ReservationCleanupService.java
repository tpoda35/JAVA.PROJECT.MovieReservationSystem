package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationCleanupService implements IReservationCleanupService {

    private final ReservationRepository reservationRepository;

    @Override
    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void deleteExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expired = reservationRepository.findByExpiresAtBeforeAndPaymentStatusNot(
                now, PaymentStatus.PAID
        );

        expired.forEach(reservation -> {
            log.info("(Cleanup Service) Deleted a reservation with the data of: {},", reservation);
            reservationRepository.delete(reservation);
        });

    }
}
