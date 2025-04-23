package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.service.IMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieAdminController {

    private final IMovieService movieService;

    @PostMapping
    public ResponseEntity<MovieDto> addMovie(
            @RequestBody @Valid MovieManageDto movieManageDto
    ) {
        log.info("addMovie :: Called endpoint. (movieManageDto:{})", movieManageDto);

        MovieDto savedMovie = movieService.addMovie(movieManageDto);
        URI location = URI.create("/movies/" + savedMovie.getId());

        return ResponseEntity.created(location).body(savedMovie);
    }

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

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(
            @PathVariable("movieId") Long movieId
    ) {
        log.info("deleteMovie :: Called endpoint. (movieId:{})", movieId);
        movieService.deleteMovie(movieId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
