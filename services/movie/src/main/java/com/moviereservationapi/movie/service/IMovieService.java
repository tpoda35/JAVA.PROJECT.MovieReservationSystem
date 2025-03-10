package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IMovieService {
    CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize);
}
