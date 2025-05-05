package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.service.ICinemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cinemas")
@Slf4j
@RequiredArgsConstructor
public class CinemaController {

    private final ICinemaService cinemaService;

    @GetMapping
    public CompletableFuture<Page<CinemaDetailsDtoV1>> getCinemas(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location
    ) {
        log.info("getCinemas :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return cinemaService.getCinemas(pageNum, pageSize, name, location);
    }

    @GetMapping("/{cinemaId}")
    public CompletableFuture<CinemaDetailsDtoV1> getCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("getCinema :: Called endpoint. (cinemaId: {})", cinemaId);

        return cinemaService.getCinema(cinemaId);
    }

}
