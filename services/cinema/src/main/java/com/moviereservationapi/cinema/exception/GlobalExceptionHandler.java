package com.moviereservationapi.cinema.exception;

import com.moviereservationapi.cinema.dto.exception.CustomExceptionDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CustomExceptionDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomExceptionDto> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        "Validation error: " + message,
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

    @ExceptionHandler(LockInterruptedException.class)
    public ResponseEntity<CustomExceptionDto> handleLockInterruptedException(
            LockInterruptedException ex
    ) {
        return ResponseEntity.status(SERVICE_UNAVAILABLE).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        SERVICE_UNAVAILABLE.value()
                )
        );
    }

    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<CustomExceptionDto> handleLockAcquisitionException(
            LockAcquisitionException ex
    ) {
        return ResponseEntity.status(CONFLICT).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        CONFLICT.value()
                )
        );
    }

    @ExceptionHandler(CinemaNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleCinemaNotFoundException(
            CinemaNotFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        NOT_FOUND.value()
                )
        );
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleRoomNotFoundException(
            RoomNotFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        NOT_FOUND.value()
                )
        );
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleSeatNotFoundException(
            SeatNotFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        NOT_FOUND.value()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionDto> handleUncaughtExceptions(Exception ex) {
        if (ex instanceof org.springframework.cache.interceptor.CacheOperationInvoker.ThrowableWrapper) {
            ex = (Exception) ((org.springframework.cache.interceptor.CacheOperationInvoker.ThrowableWrapper) ex).getOriginal();
        }

        if (ex instanceof SeatNotFoundException) {
            return handleSeatNotFoundException((SeatNotFoundException) ex);
        } else {
            return handleException(ex);
        }
    }

    private ResponseEntity<CustomExceptionDto> handleException(Exception ex) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        INTERNAL_SERVER_ERROR.value()
                )
        );
    }

}
