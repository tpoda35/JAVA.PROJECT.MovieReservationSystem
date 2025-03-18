package com.moviereservationapi.cinema.dto;

import java.time.LocalDateTime;

public record CustomExceptionDto(
        String message,
        LocalDateTime timestamp,
        Integer statusCode
){}
