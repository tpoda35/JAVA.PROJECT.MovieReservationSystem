package com.moviereservationapi.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "reservation-service", url = "http://localhost:8093")
public interface ReservationClient {

    @PostMapping("/api/reservations/feign/changeStatusToPaid/{reservationId}")
    void changeStatusToPaid(
            @PathVariable("reservationId") Long reservationId
    );

    @PostMapping("/api/reservations/feign/deleteExpiredReservationById/{reservationId}")
    void deleteExpiredReservationById(
            @PathVariable("reservationId") Long reservationId
    );
}
