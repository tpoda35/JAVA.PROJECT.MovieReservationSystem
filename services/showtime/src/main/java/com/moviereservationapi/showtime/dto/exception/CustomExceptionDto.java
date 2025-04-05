package com.moviereservationapi.showtime.dto.exception;

import java.time.LocalDateTime;

public record CustomExceptionDto(
        String message,
        LocalDateTime timestamp,
        Integer statusCode
){}
