package com.moviereservationapi.cinema.dto.exception;

import java.time.LocalDateTime;

public record CustomExceptionDto(
        String message,
        LocalDateTime timestamp,
        Integer statusCode
){}
