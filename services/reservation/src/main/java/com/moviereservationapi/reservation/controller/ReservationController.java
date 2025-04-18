package com.moviereservationapi.reservation.controller;

import com.moviereservationapi.reservation.dto.reservation.ReservationCreateDto;
import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV1;
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

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/reservations")
@Slf4j
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(
            @RequestBody ReservationCreateDto reservationCreateDto
    ) {
        log.info("addReservation :: Called endpoint. (ReservationCreateDto: {})", reservationCreateDto);

        ReservationResponseDto savedReservation = reservationService.addReservation(reservationCreateDto);
        URI location = URI.create("/seats/" + savedReservation.getId());

        return ResponseEntity.created(location).body(savedReservation);
    }

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

    @GetMapping("/user/{userId}")
    public CompletableFuture<Page<ReservationDetailsDtoV2>> getUserReservations(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("getUserReservations :: Called endpoint. (userId: {}, pageNum: {}, pageSize: {})",
                userId, pageNum, pageSize);

        return reservationService.getUserReservations(pageNum, pageSize, userId);
    }

}
