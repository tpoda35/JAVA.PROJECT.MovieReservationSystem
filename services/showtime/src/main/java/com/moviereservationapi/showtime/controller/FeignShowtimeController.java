package com.moviereservationapi.showtime.controller;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV2;
import com.moviereservationapi.showtime.service.IShowtimeFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignShowtimeController {

    private final IShowtimeFeignService showtimeFeignService;

    @GetMapping("/getShowtime/{showtimeId}")
    public ShowtimeDetailsDtoV2 getShowtime(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        return showtimeFeignService.getShowtime(showtimeId);
    }

    @PostMapping("/addShowtimeReservation/{showtimeId}/{reservationId}")
    public void addShowtimeReservation(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("reservationId") Long reservationId
    ) {
        showtimeFeignService.addShowtimeReservation(reservationId, showtimeId);
    }

}
