package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.dto.ReviewDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IMovieService {
    CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize);
    CompletableFuture<MovieDto> getMovie(Long movieId);
    // Role required.
    MovieDto addMovie(MovieManageDto movieManageDto);
    // Role required.
    MovieDto editMovie(Long movieId, MovieManageDto movieManageDto);
    // Role required.
    void deleteMovie(Long movieId);

    CompletableFuture<Page<ReviewDto>> getMovieReviews(Long movieId, int pageNum, int pageSize);
}
