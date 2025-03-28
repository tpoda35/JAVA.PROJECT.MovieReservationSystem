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
    public Boolean movieExists(Long movieId) {
        boolean exists = movieRepository.existsById(movieId);
        log.info("(Feign call) Movie with the id of {} exists: {}.", movieId, exists);
        return exists;
    }

    @Override
    @Transactional
    public void addShowtimeToMovie(Long movieId, Long showtimeId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found."));

        movie.addShowtimeId(showtimeId);
        movieRepository.save(movie);
    }
}
