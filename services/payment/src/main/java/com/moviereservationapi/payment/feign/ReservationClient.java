package com.moviereservationapi.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "reservation-service", url = "http://localhost:8093")
public interface ReservationClient {

    @PostMapping("/api/reservations/feign/changeStatusToPaid/{reservationId}")
    void changeStatusToPaid(
            @PathVariable("reservationId") Long reservationId
    );

    @PostMapping("/api/reservations/feign/changeStatusToFailed/{reservationId}")
    void changeStatusToFailed(
            @PathVariable("reservationId") Long reservationId
    );

    @GetMapping("/api/reservationseats/feign/findSeatIdsByReservationId/{reservationId}")
    List<Long> findSeatIdsByReservationId(
            @PathVariable("reservationId") Long reservationId
    );

    @PostMapping("/api/reservationseats/feign/changeStatusToUnder_Payment/{reservationId}")
    void changeStatusToUnder_Payment(
            @PathVariable("reservationId") Long reservationId
    );

}
