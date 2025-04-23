package com.moviereservationapi.showtime.controller;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeCreateDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.service.IShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
@Slf4j
public class ShowtimeAdminController {

    private final IShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ShowtimeDetailsDtoV1> addShowtime(
            @RequestBody @Valid ShowtimeCreateDto showtimeCreateDto
    ) {
        log.info("addShowtime :: Called endpoint. (showtimeManageDto:{})", showtimeCreateDto);

        ShowtimeDetailsDtoV1 savedShowtime = showtimeService.addShowtime(showtimeCreateDto);
        URI location = URI.create("/showtimes/" + savedShowtime.getId());

        return ResponseEntity.created(location).body(savedShowtime);
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
