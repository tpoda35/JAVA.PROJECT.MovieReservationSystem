package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieCreateDto;
import com.moviereservationapi.movie.dto.MovieDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IMovieService {
    CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize);
    CompletableFuture<MovieDto> getMovie(Long movieId);
    MovieDto addMovie(MovieCreateDto movieDto);
}
