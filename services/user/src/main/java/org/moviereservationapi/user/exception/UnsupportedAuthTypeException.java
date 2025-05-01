package org.moviereservationapi.user.exception;

public class UnsupportedAuthTypeException extends RuntimeException {
    public UnsupportedAuthTypeException(String message) {
        super(message);
    }
}
