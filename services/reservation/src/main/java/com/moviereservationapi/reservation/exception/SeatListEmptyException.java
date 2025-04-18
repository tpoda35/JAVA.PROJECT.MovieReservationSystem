package com.moviereservationapi.reservation.exception;

public class SeatListEmptyException extends RuntimeException {
    public SeatListEmptyException(String message) {
        super(message);
    }
}
