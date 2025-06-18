package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.model.Reservation;
import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.moviereservationapi.reservation.Enum.PaymentStatus.PENDING;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationCleanupService implements IReservationCleanupService {

    private final ReservationRepository reservationRepository;

    @Override
    @Scheduled(cron = "0 0/15 * * * *")
    public void deleteExpiredReservations() {
        String LOG_PREFIX = "deleteExpiredReservations";
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expired = reservationRepository.findByExpiresAtBeforeAndPaymentStatus(
                now, PENDING
        );

        log.info("{} :: Deleting {} expired reservation.", LOG_PREFIX, expired.size());
        expired.forEach(reservation -> {
            log.info("(Cleanup Service) Deleted a reservation with the data of: {}.", reservation);
            reservationRepository.delete(reservation);
        });
    }
}
