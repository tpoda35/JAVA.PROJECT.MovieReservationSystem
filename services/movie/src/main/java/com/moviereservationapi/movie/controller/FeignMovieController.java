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

    @PostMapping("/addShowtimeToMovie/{showtimeId}/{movieId}")
    public void addShowtimeToMovie(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("movieId") Long movieId
    ) {
        log.info("(Feign call) Adding showtime to the movie with the id of {}.", movieId);

        movieFeignService.addShowtimeToMovie(showtimeId, movieId);
    }

    @DeleteMapping("/deleteShowtimeFromMovie/{showtimeId}/{movieId}")
    public void deleteShowtimeFromMovie(
         @PathVariable("showtimeId") Long showtimeId,
         @PathVariable("movieId") Long movieId
    ) {
        log.info("(Feign call) Removing showtime with the id of {} from the movie with the id of {}.", showtimeId, movieId);

        movieFeignService.deleteShowtimeFromMovie(showtimeId, movieId);
    }

}
