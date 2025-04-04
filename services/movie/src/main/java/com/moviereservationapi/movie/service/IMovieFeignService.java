package com.moviereservationapi.movie.service;

public interface IMovieFeignService {
    void addShowtimeToMovie(Long showtimeId, Long movieId);
}
