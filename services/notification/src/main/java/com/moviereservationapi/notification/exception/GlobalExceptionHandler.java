package com.moviereservationapi.notification.exception;

import com.moviereservationapi.notification.dto.exception.CustomExceptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomExceptionDto> handleUncaughtExceptions(Exception ex) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        INTERNAL_SERVER_ERROR.value()
                )
        );
    }

}
