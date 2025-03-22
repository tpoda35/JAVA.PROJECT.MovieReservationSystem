package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service", url = "http://localhost:8090")
public interface MovieClient {

    @GetMapping("/api/movies/feign/movieExists/{movieId}")
    Boolean movieExists(@PathVariable("movieId") Long movieId);

}
