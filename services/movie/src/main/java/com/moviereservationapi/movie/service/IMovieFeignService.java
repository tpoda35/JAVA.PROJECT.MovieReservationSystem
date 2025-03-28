package com.moviereservationapi.movie.service;

public interface IMovieFeignService {
    Boolean movieExists(Long movieId);
    void addShowtimeToMovie(Long movieId, Long showtimeId);
}
