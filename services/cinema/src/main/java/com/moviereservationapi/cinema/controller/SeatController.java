package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.service.ISeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
