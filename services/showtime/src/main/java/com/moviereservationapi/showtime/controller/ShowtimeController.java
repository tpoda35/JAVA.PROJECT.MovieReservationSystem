package com.moviereservationapi.showtime.controller;

import com.moviereservationapi.showtime.dto.ShowtimeDto;
import com.moviereservationapi.showtime.dto.ShowtimeManageDto;
import com.moviereservationapi.showtime.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
@Slf4j
public class ShowtimeController {

    private final IShowtimeService showtimeService;

    @GetMapping
    public CompletableFuture<Page<ShowtimeDto>> getShowtimes(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("api/showtimes :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return showtimeService.getShowtimes(pageNum, pageSize);
    }

    @GetMapping("/{showtimeId}")
    public CompletableFuture<ShowtimeDto> getShowtime(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        log.info("api/showtimes/showtimeId :: Called endpoint. (showtimeId:{})", showtimeId);

        return showtimeService.getShowtime(showtimeId);
    }

    @PostMapping
    public ResponseEntity<ShowtimeDto> addShowtime(
            @RequestBody @Valid ShowtimeManageDto showtimeManageDto
    ) {
        log.info("api/showtimes (addShowtime) :: Called endpoint. (showtimeManageDto:{})", showtimeManageDto);

        ShowtimeDto savedShowtime = showtimeService.addShowtime(showtimeManageDto);
        URI location = URI.create("/showtimes/" + savedShowtime.getId());

        return ResponseEntity.created(location).body(savedShowtime);
    }

}
