package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatManageDto;
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
    public CompletableFuture<SeatDto> getSeat(
            @PathVariable("seatId") Long seatId
    ) {
        log.info("api/seats/seatId :: Called endpoint. (seatId:{})", seatId);

        return seatService.getSeat(seatId);
    }

    @GetMapping("/room/{roomId}")
    public CompletableFuture<List<SeatDto>> getAllSeatByRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("api/seats/room/roomId :: Called endpoint. (roomId:{})", roomId);

        return seatService.getAllSeatByRoom(roomId);
    }

    @PostMapping
    public ResponseEntity<SeatDto> addSeat(
            @Valid @RequestBody SeatManageDto seatManageDto
    ) {
        log.info("api/seats (addSeat) :: Called endpoint. (seatManageDto:{})", seatManageDto);

        SeatDto savedSeat = seatService.addSeat(seatManageDto);
        URI location = URI.create("/seats/" + savedSeat.getId());

        return ResponseEntity.created(location).body(savedSeat);
    }

    @PutMapping("/{seatId}")
    public SeatDto editSeat(
            @PathVariable("seatId") Long seatId,
            @RequestBody @Valid SeatManageDto seatManageDto
    ) {
        log.info("api/seats/seatId (editSeat) :: Called endpoint. (seatManageDto:{})", seatManageDto);

        return seatService.editSeat(seatId, seatManageDto);
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable("seatId") Long seatId
    ) {
        log.info("api/seats/seatId (deleteSeat) :: Called endpoint. (seatId:{})", seatId);

        seatService.deleteSeat(seatId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
