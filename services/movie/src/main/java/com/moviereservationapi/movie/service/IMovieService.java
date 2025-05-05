package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.Enum.MovieGenre;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface IMovieService {
    CompletableFuture<Page<MovieDto>> getMovies(
            int pageNum,
            int pageSize,
            String title,
            LocalDateTime releaseAfter,
            Double durationGreaterThan,
            MovieGenre movieGenre
    );
    CompletableFuture<MovieDto> getMovie(Long movieId);
    MovieDto addMovie(MovieManageDto movieManageDto);
    MovieDto editMovie(Long movieId, MovieManageDto movieManageDto);
    void deleteMovie(Long movieId);
}
