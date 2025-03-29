package com.moviereservationapi.cinema.exception;

public class CinemaNotFoundException extends RuntimeException {
    public CinemaNotFoundException(String message) {
        super(message);
    }
}
