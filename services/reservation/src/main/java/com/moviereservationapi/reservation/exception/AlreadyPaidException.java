package com.moviereservationapi.reservation.exception;

public class AlreadyPaidException extends RuntimeException {
    public AlreadyPaidException(String message) {
        super(message);
    }
}
