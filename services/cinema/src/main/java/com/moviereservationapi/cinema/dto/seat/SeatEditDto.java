package com.moviereservationapi.cinema.dto.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatEditDto {

    @NotBlank(message = "Seat row field cannot be empty.")
    private String seatRow;

    @NotNull(message = "Seat number field cannot be empty.")
    private Integer seatNumber;

}
