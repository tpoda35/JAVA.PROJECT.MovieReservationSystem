package com.moviereservationapi.showtime.controller;

import com.moviereservationapi.showtime.dto.seat.SeatAvailabilityDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeCreateDto;
import com.moviereservationapi.showtime.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
@Slf4j
public class ShowtimeController {

    private final IShowtimeService showtimeService;

    @GetMapping
    public CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimes(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("getShowtimes :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return showtimeService.getShowtimes(pageNum, pageSize);
    }

    @GetMapping("/{showtimeId}")
    public CompletableFuture<ShowtimeDetailsDtoV1> getShowtime(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        log.info("getShowtime :: Called endpoint. (showtimeId:{})", showtimeId);

        return showtimeService.getShowtime(showtimeId);
    }

    @PostMapping
    public ResponseEntity<ShowtimeDetailsDtoV1> addShowtime(
            @RequestBody @Valid ShowtimeCreateDto showtimeCreateDto
    ) {
        log.info("addShowtime :: Called endpoint. (showtimeManageDto:{})", showtimeCreateDto);

        ShowtimeDetailsDtoV1 savedShowtime = showtimeService.addShowtime(showtimeCreateDto);
        URI location = URI.create("/showtimes/" + savedShowtime.getId());

        return ResponseEntity.created(location).body(savedShowtime);
    }

    @GetMapping("/movie/{movieId}")
    public CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimesByMovie(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @PathVariable("movieId") Long movieId
    ) {
        log.info("getShowtimesByMovie :: Called endpoint. (pageNum:{}, pageSize:{}, movieId:{})", pageNum, pageSize, movieId);

        return showtimeService.getShowtimesByMovie(movieId, pageNum, pageSize);
    }

    @GetMapping("/{showtimeId}/seats")
    public CompletableFuture<List<SeatAvailabilityDto>> getSeatsByShowtime(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        log.info("getSeatsByShowtime :: Called endpoint. (showtimeId:{})", showtimeId);

        return showtimeService.getSeatsByShowtime(showtimeId);
    }

    @DeleteMapping("/{showtimeId}")
    public ResponseEntity<Void> deleteShowtime(
        @PathVariable("showtimeId") Long showtimeId
    ) {
        log.info("deleteShowtime :: Called endpoint. (showtimeId:{})", showtimeId);
        showtimeService.deleteShowtime(showtimeId);

        return ResponseEntity.noContent().build();
    }

}
