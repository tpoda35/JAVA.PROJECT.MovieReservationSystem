package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "movie-service", url = "http://localhost:8090")
public interface MovieClient {

    @GetMapping("/api/movies/feign/movieExists/{movieId}")
    Boolean movieExists(@PathVariable("movieId") Long movieId);

    @PostMapping("/api/movies/feign/addShowtimeToMovie/{movieId}/{showtimeId}")
    void addShowtimeToMovie(
            @PathVariable("movieId") Long movieId,
            @PathVariable("showtimeId") Long showtimeId
    );
}
