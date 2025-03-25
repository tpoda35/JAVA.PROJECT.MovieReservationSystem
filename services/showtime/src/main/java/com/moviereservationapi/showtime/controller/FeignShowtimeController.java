package com.moviereservationapi.showtime.controller;

import com.moviereservationapi.showtime.service.IShowtimeFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/showtimes/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignShowtimeController {

    private final IShowtimeFeignService showtimeFeignService;

    @GetMapping("/showtimeExists/{showtimeId}")
    public Boolean showtimeExists(
            @PathVariable("showtimeId") Long showtimeId
    ) {
        return showtimeFeignService.showtimeExists(showtimeId);
    }

}
