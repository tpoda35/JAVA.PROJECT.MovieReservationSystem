package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
import com.moviereservationapi.reservation.service.IReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/admin/reservations")
@Slf4j
@RequiredArgsConstructor
public class ReservationAdminController {

    private final IReservationService reservationService;

    @GetMapping("/{reservationId}")
    public CompletableFuture<ReservationDetailsDtoV1> getReservation(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("getReservation :: Called endpoint. (reservationId: {})", reservationId);

        return reservationService.getReservation(reservationId);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("reservationId") Long reservationId
    ) {
        log.info("deleteReservation :: Called endpoint. (reservationId: {})", reservationId);

        reservationService.deleteReservation(reservationId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

}
