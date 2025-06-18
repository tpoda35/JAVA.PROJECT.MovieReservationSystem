package com.moviereservationapi.reservation.exception;

import com.moviereservationapi.reservation.dto.exception.CustomExceptionDto;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(SeatLimitExceededException.class)
    public ResponseEntity<CustomExceptionDto> handleSeatLimitExceededException(SeatLimitExceededException ex) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

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

    @ExceptionHandler(AlreadyPaidException.class)
    public ResponseEntity<CustomExceptionDto> handleAlreadyPaidException(
            AlreadyPaidException ex
    ) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
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

    @ExceptionHandler(InvalidSeatRoomException.class)
    public ResponseEntity<CustomExceptionDto> handleInvalidSeatRoomException(
            InvalidSeatRoomException ex
    ) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

    @ExceptionHandler(SeatAlreadyReservedException.class)
    public ResponseEntity<CustomExceptionDto> handleSeatAlreadyReservedException(
            SeatAlreadyReservedException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        NOT_FOUND.value()
                )
        );
    }

    @ExceptionHandler(SeatListEmptyException.class)
    public ResponseEntity<CustomExceptionDto> handleSeatListEmptyException(
            SeatListEmptyException ex
    ) {
        return ResponseEntity.status(BAD_REQUEST).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        BAD_REQUEST.value()
                )
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleUserNotFoundException(
            UserNotFoundException ex
    ) {
        return ResponseEntity.status(NOT_FOUND).body(
                new CustomExceptionDto(
                        ex.getMessage(),
                        LocalDateTime.now(),
                        NOT_FOUND.value()
                )
        );
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<CustomExceptionDto> handleReservationNotFoundException(
            ReservationNotFoundException ex
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

        if (ex instanceof ReservationNotFoundException) {
            return handleReservationNotFoundException((ReservationNotFoundException) ex);
        } else if (ex instanceof UserNotFoundException) {
            return handleUserNotFoundException((UserNotFoundException) ex);
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
