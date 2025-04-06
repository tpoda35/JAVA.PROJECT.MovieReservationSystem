package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.service.IReservationFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
