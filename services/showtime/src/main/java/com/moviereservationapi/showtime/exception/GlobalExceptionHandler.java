package com.moviereservationapi.showtime.exception;

import com.moviereservationapi.showtime.dto.CustomExceptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleMovieNotFoundException(
            ShowtimeNotFoundException ex
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

        if (ex instanceof ShowtimeNotFoundException) {
            return handleMovieNotFoundException((ShowtimeNotFoundException) ex);
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
