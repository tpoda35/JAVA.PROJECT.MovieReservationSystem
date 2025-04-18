package com.moviereservationapi.reservation.dto.reservation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDto {

    @NotNull(message = "ShowtimeId field cannot be empty.")
    private Long showtimeId;

    @NotNull(message = "UserId field cannot be empty.")
    private Long userId;

    @NotNull(message = "SeatIds field is required.")
    private List<Long> seatIds;

}
