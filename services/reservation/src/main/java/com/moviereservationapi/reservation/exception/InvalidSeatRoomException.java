package com.moviereservationapi.reservation.exception;

public class InvalidSeatRoomException extends RuntimeException {
    public InvalidSeatRoomException(String message) {
        super(message);
    }
}
