package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.dto.MovieCreateDto;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.service.IMovieService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final IMovieService movieService;

    @GetMapping
    public CompletableFuture<Page<MovieDto>> getAllMovie(
            @RequestParam(defaultValue = "0") @NonNull Integer pageNum,
            @RequestParam(defaultValue = "10") @NonNull Integer pageSize
    ){
        log.info("api/movies :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return movieService.getAllMovie(pageNum, pageSize);
    }

    @GetMapping("/{movieId}")
    public CompletableFuture<MovieDto> getMovie(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("api/movies/movieId :: Called endpoint. (movieId:{})", movieId);

        return movieService.getMovie(movieId);
    }

    // Role required endpoints.
    @PostMapping("/addMovie")
    public ResponseEntity<MovieDto> addMovie(
            @RequestBody @Valid MovieCreateDto movieCreateDto
    ) {
        log.info("api/movies/addMovie :: Called endpoint. (movieDto:{})", movieCreateDto);

        MovieDto savedMovie = movieService.addMovie(movieCreateDto);
        URI location = URI.create("/movies/" + savedMovie.getId());

        return ResponseEntity.created(location).body(savedMovie);
    }

}
