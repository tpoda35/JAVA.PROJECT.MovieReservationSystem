package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.service.IReservationFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations/feign")
@Slf4j
@RequiredArgsConstructor
public class FeignReservationController {

    private final IReservationFeignService reservationFeignService;

    @DeleteMapping("/deleteReservationWithShowtimeId/{showtimeId}")
    public void deleteReservationWithShowtimeId(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        log.info("(Feign call) Deleting Reservations and ReservationSeats associated with showtime with the id of {}.", showtimeId);

        reservationFeignService.deleteReservationWithShowtimeId(showtimeId);
    }

    @PostMapping("/changeStatusToPaid/{reservationId}")
    public void changeStatusToPaid(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("(Feign call) Changing reservation status to PAID with the id of {}.", reservationId);

        reservationFeignService.changeStatusToPaid(reservationId);
    }

    @PostMapping("/changeStatusToFailed/{reservationId}")
    public void changeStatusToFailed(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("(Feign call) Changing reservation status to FAILED with the id of {}.", reservationId);

        reservationFeignService.changeStatusToFailed(reservationId);
    }

    @PostMapping("/changeStatusToUnder_Payment/{reservationId}")
    public void changeStatusToUnder_Payment(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("(Feign call) Changing reservation status to UNDER_PAYMENT with the id of {}.", reservationId);

        reservationFeignService.changeStatusToUnder_Payment(reservationId);
    }

}
