package com.moviereservationapi.showtime.dto.seat;

public record SeatAvailabilityDto(
        Long seatId,
        String seatRow,
        Integer seatNumber,
        Boolean available
) {
}
