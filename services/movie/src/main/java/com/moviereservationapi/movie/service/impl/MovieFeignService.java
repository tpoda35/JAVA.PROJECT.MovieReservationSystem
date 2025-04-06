package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.MovieRepository;
import com.moviereservationapi.movie.service.IMovieFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieFeignService implements IMovieFeignService {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public void addShowtimeToMovie(Long showtimeId, Long movieId) {
        Movie movie = findMovieById(movieId);
        movie.getShowtimeIds().add(showtimeId);
        movieRepository.save(movie);
    }

    @Override
    @Transactional
    public void deleteShowtimeFromMovie(Long showtimeId, Long movieId) {
        Movie movie = findMovieById(movieId);

        if (movie.getShowtimeIds().remove(showtimeId)) {
            movieRepository.save(movie);
        } else {
            log.info("(Feign call) Showtime(id:{}) associated with the Movie(id:{}) not found.", showtimeId, movieId);
            throw new IllegalArgumentException("Showtime ID not found in movie");
        }
    }

    private Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Movie with the id of {} not found.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
    }
}
