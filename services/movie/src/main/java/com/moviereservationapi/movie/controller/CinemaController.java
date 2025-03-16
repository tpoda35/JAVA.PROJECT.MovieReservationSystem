package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.dto.CinemaDto;
import com.moviereservationapi.movie.service.ICinemaService;
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
    public CompletableFuture<Page<CinemaDto>> getAllCinema(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("api/cinemas :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return cinemaService.getAllCinema(pageNum,pageSize);
    }

    @GetMapping("/{cinemaId}")
    public CompletableFuture<CinemaDto> getCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("api/cinemas/cinemaId :: Called endpoint. (cinemaId:{})", cinemaId);

        return cinemaService.getCinema(cinemaId);
    }

}
