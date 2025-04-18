package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.dto.reservation.ReservationPayment;
import com.moviereservationapi.reservation.service.IReservationSeatFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservationseats/feign")
@Slf4j
@RequiredArgsConstructor
public class FeignReservationSeatController {

    private final IReservationSeatFeignService reservationSeatFeignService;

    @GetMapping("/findReservedSeatIdsByShowtimeId/{showtimeId}")
    public List<Long> findReservedSeatIdsByShowtimeId(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        return reservationSeatFeignService.findReservedSeatIdsByShowtimeId(showtimeId);
    }

}
