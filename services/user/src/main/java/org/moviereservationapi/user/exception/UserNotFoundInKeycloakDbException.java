package org.moviereservationapi.user.exception;

public class UserNotFoundInKeycloakDbException extends RuntimeException {
    public UserNotFoundInKeycloakDbException(String message) {
        super(message);
    }
}
