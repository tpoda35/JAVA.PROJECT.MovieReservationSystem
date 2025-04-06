package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "movie-service", url = "http://localhost:8090")
public interface MovieClient {

    @PostMapping("/api/movies/feign/addShowtimeToMovie/{showtimeId}/{movieId}")
    void addShowtimeToMovie(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("movieId") Long movieId
    );

    @DeleteMapping("/api/movies/feign/deleteShowtimeFromMovie/{showtimeId}/{movieId}")
    void deleteShowtimeFromMovie(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("movieId") Long movieId
    );
}
