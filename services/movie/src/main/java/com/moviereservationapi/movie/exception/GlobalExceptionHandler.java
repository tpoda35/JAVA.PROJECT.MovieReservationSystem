package com.moviereservationapi.movie.exception;

import com.moviereservationapi.movie.dto.CustomExceptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockAcquisitionException.class)
    public ResponseEntity<CustomExceptionDto> handleLockAcquisitionException(
            LockAcquisitionException ex
    ) {
        return ResponseEntity.status(LOCKED).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        LOCKED.value()
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

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleMovieNotFoundException(
            MovieNotFoundException ex
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

        if (ex instanceof MovieNotFoundException) {
            return handleMovieNotFoundException((MovieNotFoundException) ex);
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
