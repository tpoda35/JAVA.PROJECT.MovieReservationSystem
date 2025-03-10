package com.moviereservationapi.movie.mapper;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.model.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MovieMapper {

    MovieMapper INSTANCE = Mappers.getMapper(MovieMapper.class);

    List<MovieDto> toDto(List<Movie> movies);

}
