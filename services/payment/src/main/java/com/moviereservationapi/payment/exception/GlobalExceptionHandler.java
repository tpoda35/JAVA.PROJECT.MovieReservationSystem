package com.moviereservationapi.payment.exception;

import com.moviereservationapi.payment.dto.exception.CustomExceptionDto;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<CustomExceptionDto> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

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
