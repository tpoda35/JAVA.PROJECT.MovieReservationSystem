package com.moviereservationapi.movie.mapper;

import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.model.Movie;

public class MovieMapper {
    
    public static MovieDto fromMovieToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .length(movie.getDuration())
                .release(movie.getReleaseDate())
                .movieGenre(movie.getMovieGenre())
                .build();
    }

    public static Movie fromManageDtoToMovie(MovieManageDto movieDto) {
        return Movie.builder()
                .title(movieDto.getTitle())
                .duration(movieDto.getLength())
                .releaseDate(movieDto.getRelease())
                .movieGenre(movieDto.getMovieGenre())
                .build();
    }
}
