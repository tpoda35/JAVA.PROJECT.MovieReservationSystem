package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.dto.ReservationManageDto;
import com.moviereservationapi.reservation.dto.ReservationResponseDto;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @PostMapping
    public ReservationResponseDto addReservation(
            @RequestBody ReservationManageDto reservationManageDto
    ) {
        return reservationService.addReservation(reservationManageDto);
    }

}
