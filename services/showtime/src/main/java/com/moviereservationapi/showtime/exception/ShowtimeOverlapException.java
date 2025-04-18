package com.moviereservationapi.showtime.exception;

public class ShowtimeOverlapException extends RuntimeException {
    public ShowtimeOverlapException(String message) {
        super(message);
    }
}
