package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.service.IMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    public CompletableFuture<Page<MovieDto>> getMovies(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        log.info("getMovies :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return movieService.getMovies(pageNum, pageSize);
    }

    @GetMapping("/{movieId}")
    public CompletableFuture<MovieDto> getMovie(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("getMovie :: Called endpoint. (movieId:{})", movieId);

        return movieService.getMovie(movieId);
    }

    // Role required endpoint
    @PostMapping
    public ResponseEntity<MovieDto> addMovie(
            @RequestBody @Valid MovieManageDto movieManageDto
    ) {
        log.info("addMovie :: Called endpoint. (movieManageDto:{})", movieManageDto);

        MovieDto savedMovie = movieService.addMovie(movieManageDto);
        URI location = URI.create("/movies/" + savedMovie.getId());

        return ResponseEntity.created(location).body(savedMovie);
    }

    // Role required endpoint
    @PutMapping("/{movieId}")
    public MovieDto editMovie(
            @PathVariable("movieId") Long movieId,
            @RequestBody @Valid MovieManageDto movieManageDto
    ) {
        log.info("editMovie :: Called endpoint. (movieManageDto:{}, movieId:{})",
                movieManageDto, movieId
        );

        return movieService.editMovie(movieId, movieManageDto);
    }

    // Role required endpoint
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("deleteMovie :: Called endpoint. (movieId:{})", movieId);
        movieService.deleteMovie(movieId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
