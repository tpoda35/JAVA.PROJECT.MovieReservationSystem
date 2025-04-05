package com.moviereservationapi.reservation.exception;

public class ReservationSeatNotFoundException extends RuntimeException {
    public ReservationSeatNotFoundException(String message) {
        super(message);
    }
}
