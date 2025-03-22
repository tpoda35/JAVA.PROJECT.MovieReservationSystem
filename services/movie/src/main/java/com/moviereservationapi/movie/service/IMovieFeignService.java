package com.moviereservationapi.movie.service;

public interface IMovieFeignService {
    Boolean movieExists(Long movieId);
}
