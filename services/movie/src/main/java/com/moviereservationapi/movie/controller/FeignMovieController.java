package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.service.IMovieFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies/feign")
@Slf4j
@RequiredArgsConstructor
public class FeignMovieController {

    private final IMovieFeignService movieFeignService;

    @GetMapping("/movieExists/{movieId}")
    public Boolean movieExists(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("(Feign call) Checking movie existence with the id of {}.", movieId);

        return movieFeignService.movieExists(movieId);
    }

    @PostMapping("/addShowtimeToMovie/{movieId}/{showtimeId}")
    public void addShowtimeToMovie(
            @PathVariable("movieId") Long movieId,
            @PathVariable("showtimeId") Long showtimeId
    ) {
        movieFeignService.addShowtimeToMovie(movieId, showtimeId);
    }

}
