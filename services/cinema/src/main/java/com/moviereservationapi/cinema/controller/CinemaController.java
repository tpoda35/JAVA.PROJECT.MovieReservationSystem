package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.service.ICinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("api/cinemas :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return cinemaService.getCinemas(pageNum, pageSize);
    }

    @GetMapping("/{cinemaId}")
    public CompletableFuture<CinemaDetailsDtoV1> getCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("api/cinemas/cinemaId :: Called endpoint. (cinemaId: {})", cinemaId);

        return cinemaService.getCinema(cinemaId);
    }

    @PostMapping
    public CinemaDetailsDtoV1 addCinema(
            @RequestBody @Valid CinemaManageDto cinemaManageDto
    ) {
        log.info("api/cinemas (addCinema) :: Called endpoint. (cinemaManageDto: {})", cinemaManageDto);

        return cinemaService.addCinema(cinemaManageDto);
    }

    @PutMapping("/{cinemaId}")
    public CinemaDetailsDtoV2 editCinema(
            @RequestBody @Valid CinemaManageDto cinemaManageDto,
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("api/cinemas (editCinema) :: Called endpoint. (cinemaManageDto: {}, cinemaId: {})", cinemaManageDto, cinemaId);

        return cinemaService.editCinema(cinemaManageDto, cinemaId);
    }

    @DeleteMapping("/{cinemaId}")
    public ResponseEntity<Void> deleteCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("api/cinemas/cinemaId (deleteMovie) :: Called endpoint. (cinemaId:{})", cinemaId);
        cinemaService.deleteCinema(cinemaId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
