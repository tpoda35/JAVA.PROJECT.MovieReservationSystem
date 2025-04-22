package com.moviereservationapi.showtime.exception;

import com.moviereservationapi.showtime.dto.exception.CustomExceptionDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<CustomExceptionDto> handleFeignStatusException(FeignException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.status());

        return ResponseEntity.status(status).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        status.value()
                )
        );
    }

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

    @ExceptionHandler(ShowtimeOverlapException.class)
    public ResponseEntity<CustomExceptionDto> handleShowtimeOverlapException(
            ShowtimeOverlapException ex
    ) {
        return ResponseEntity.status(CONFLICT).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        CONFLICT.value()
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

    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleShowtimeNotFoundException(
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
            return handleShowtimeNotFoundException((ShowtimeNotFoundException) ex);
        } else if (ex instanceof MovieNotFoundException) {
            return handleMovieNotFoundException((MovieNotFoundException) ex);
        } else if (ex instanceof RoomNotFoundException) {
            return handleRoomNotFoundException((RoomNotFoundException) ex);
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
