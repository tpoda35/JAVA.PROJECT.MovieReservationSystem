package com.moviereservationapi.reservation.exception;

public class LockInterruptedException extends RuntimeException {
    public LockInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
