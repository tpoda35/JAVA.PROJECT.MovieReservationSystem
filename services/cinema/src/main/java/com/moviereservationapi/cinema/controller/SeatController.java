package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.seat.SeatCreateDto;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatEditDto;
import com.moviereservationapi.cinema.service.ISeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/seats")
@Slf4j
@RequiredArgsConstructor
public class SeatController {

    private final ISeatService seatService;

    @GetMapping("/{seatId}")
    public CompletableFuture<SeatDetailsDtoV1> getSeatById(
            @PathVariable("seatId") Long seatId
    ) {
        log.info("getSeatById :: Called endpoint. (seatId:{})", seatId);

        return seatService.getSeatById(seatId);
    }

    @GetMapping("/room/{roomId}")
    public CompletableFuture<List<SeatDetailsDtoV1>> getAllSeatByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("getAllSeatByRoom :: Called endpoint. (roomId:{})", roomId);

        return seatService.getAllSeatByRoom(roomId);
    }

    @PostMapping
    public ResponseEntity<SeatDetailsDtoV1> addSeat(
            @Valid @RequestBody SeatCreateDto seatCreateDto
    ) {
        log.info("addSeat :: Called endpoint. (SeatCreateDto:{})", seatCreateDto);

        SeatDetailsDtoV1 savedSeat = seatService.addSeat(seatCreateDto);
        URI location = URI.create("/seats/" + savedSeat.getId());

        return ResponseEntity.created(location).body(savedSeat);
    }

    @PutMapping("/{seatId}")
    public SeatDetailsDtoV1 editSeat(
            @PathVariable("seatId") Long seatId,
            @RequestBody @Valid SeatEditDto seatEditDto
    ) {
        log.info("editSeat :: Called endpoint. (seatManageDto:{})", seatEditDto);

        return seatService.editSeat(seatId, seatEditDto);
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable("seatId") Long seatId
    ) {
        log.info("deleteSeat :: Called endpoint. (seatId:{})", seatId);

        seatService.deleteSeat(seatId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
