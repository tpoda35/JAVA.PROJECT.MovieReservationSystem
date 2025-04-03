package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IMovieService {
    CompletableFuture<Page<MovieDto>> getMovies(int pageNum, int pageSize);
    CompletableFuture<MovieDto> getMovie(Long movieId);
    MovieDto addMovie(MovieManageDto movieManageDto);
    MovieDto editMovie(Long movieId, MovieManageDto movieManageDto);
    void deleteMovie(Long movieId);
}
