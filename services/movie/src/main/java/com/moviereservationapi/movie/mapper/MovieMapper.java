package com.moviereservationapi.movie.mapper;

import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.model.Movie;

public class MovieMapper {

    public static Movie fromDtoToMovie(MovieDto movieDto) {
        return Movie.builder()
                .id(movieDto.getId())
                .title(movieDto.getTitle())
                .length(movieDto.getLength())
                .release(movieDto.getRelease())
                .movieGenre(movieDto.getMovieGenre())
                .build();
    }

    public static MovieDto fromMovieToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .length(movie.getLength())
                .release(movie.getRelease())
                .movieGenre(movie.getMovieGenre())
                .build();
    }

    public static Movie fromManageDtoToMovie(MovieManageDto movieDto) {
        return Movie.builder()
                .title(movieDto.getTitle())
                .length(movieDto.getLength())
                .release(movieDto.getRelease())
                .movieGenre(movieDto.getMovieGenre())
                .build();
    }
}
