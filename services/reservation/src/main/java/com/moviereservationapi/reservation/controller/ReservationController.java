package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.dto.reservation.ReservationCreateDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import com.moviereservationapi.reservation.dto.reservation.ReservationResponseDto;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservationAndCreateCheckoutUrl(
            @RequestBody ReservationCreateDto reservationCreateDto
    ) {
        log.info("addReservationAndCreateCheckoutUrl :: Called endpoint. (ReservationCreateDto: {})", reservationCreateDto);

        ReservationResponseDto savedReservation = reservationService.addReservationAndCreateCheckoutUrl(reservationCreateDto);
        URI location = URI.create("/seats/" + savedReservation.getId());

        return ResponseEntity.created(location).body(savedReservation);
    }

    @GetMapping("/user")
    public CompletableFuture<Page<ReservationDetailsDtoV2>> getLoggedInUserReservations(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("getLoggedInUserReservations :: Called endpoint. (pageNum: {}, pageSize: {})",
                pageNum, pageSize);

        return reservationService.getLoggedInUserReservations(pageNum, pageSize);
    }

}
