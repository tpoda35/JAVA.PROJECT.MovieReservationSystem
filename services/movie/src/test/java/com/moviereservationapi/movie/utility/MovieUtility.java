package com.moviereservationapi.movie.utility;

import com.moviereservationapi.movie.dto.MovieDto;

import java.time.LocalDateTime;

import static com.moviereservationapi.movie.Enum.MovieGenre.ACTION;

public class MovieUtility {

    public static MovieDto getMovieDto() {
        return MovieDto.builder()
                .id(1L)
                .title("Batman")
                .length(160.0)
                .release(LocalDateTime.now())
                .movieGenre(ACTION)
                .build();
    }

}
