package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.cinema.CinemaManageDto;
import com.moviereservationapi.cinema.service.ICinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
        log.info("getCinemas :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return cinemaService.getCinemas(pageNum, pageSize);
    }

    @GetMapping("/{cinemaId}")
    public CompletableFuture<CinemaDetailsDtoV1> getCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("getCinema :: Called endpoint. (cinemaId: {})", cinemaId);

        return cinemaService.getCinema(cinemaId);
    }

    @PostMapping
    public ResponseEntity<CinemaDetailsDtoV1> addCinema(
            @RequestBody @Valid CinemaManageDto cinemaManageDto
    ) {
        log.info("addCinema :: Called endpoint. (cinemaManageDto: {})", cinemaManageDto);

        CinemaDetailsDtoV1 savedCinema = cinemaService.addCinema(cinemaManageDto);
        URI location = URI.create("/seats/" + savedCinema.getId());

        return ResponseEntity.created(location).body(savedCinema);
    }

    @PutMapping("/{cinemaId}")
    public CinemaDetailsDtoV2 editCinema(
            @RequestBody @Valid CinemaManageDto cinemaManageDto,
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("editCinema :: Called endpoint. (cinemaManageDto: {}, cinemaId: {})", cinemaManageDto, cinemaId);

        return cinemaService.editCinema(cinemaManageDto, cinemaId);
    }

    @DeleteMapping("/{cinemaId}")
    public ResponseEntity<Void> deleteCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {
        log.info("deleteCinema :: Called endpoint. (cinemaId:{})", cinemaId);
        cinemaService.deleteCinema(cinemaId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
