package org.moviereservationapi.user.dto;

import java.time.LocalDateTime;

public record CustomExceptionDto(
        String message,
        LocalDateTime timestamp,
        Integer statusCode
) {
}
