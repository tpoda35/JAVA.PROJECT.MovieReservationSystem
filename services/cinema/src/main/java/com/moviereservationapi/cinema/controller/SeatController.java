package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.service.ISeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
