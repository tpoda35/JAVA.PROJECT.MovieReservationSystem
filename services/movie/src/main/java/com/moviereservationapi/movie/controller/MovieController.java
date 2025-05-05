package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.Enum.MovieGenre;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.service.IMovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final IMovieService movieService;

    @GetMapping
    public CompletableFuture<Page<MovieDto>> getMovies(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDateTime releaseAfter,
            @RequestParam(required = false) Double durationGreaterThan,
            @RequestParam(required = false) MovieGenre movieGenre
    ){
        log.info("getMovies :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return movieService.getMovies(pageNum, pageSize, name, releaseAfter, durationGreaterThan, movieGenre);
    }

    @GetMapping("/{movieId}")
    public CompletableFuture<MovieDto> getMovie(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("getMovie :: Called endpoint. (movieId:{})", movieId);

        return movieService.getMovie(movieId);
    }
}
