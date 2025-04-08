package com.moviereservationapi.reservation.exception;

public class SeatListEmtpyException extends RuntimeException {
    public SeatListEmtpyException(String message) {
        super(message);
    }
}
