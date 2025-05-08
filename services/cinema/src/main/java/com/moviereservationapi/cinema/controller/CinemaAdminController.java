package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.cinema.CinemaManageDto;
import com.moviereservationapi.cinema.service.ICinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/cinemas")
@Slf4j
@RequiredArgsConstructor
public class CinemaAdminController {

    private final ICinemaService cinemaService;

    @PostMapping
    public ResponseEntity<CinemaDetailsDtoV1> addCinema(
            @RequestBody @Valid CinemaManageDto cinemaManageDto
    ) {
        log.info("addCinema :: Called endpoint. (cinemaManageDto: {})", cinemaManageDto);

        CinemaDetailsDtoV1 savedCinema = cinemaService.addCinema(cinemaManageDto);
        URI location = URI.create("/cinemas/" + savedCinema.getId());

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
