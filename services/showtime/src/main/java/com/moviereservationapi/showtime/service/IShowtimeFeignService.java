package com.moviereservationapi.showtime.service;

public interface IShowtimeFeignService {
    Boolean showtimeExists(Long showtimeId);
}
