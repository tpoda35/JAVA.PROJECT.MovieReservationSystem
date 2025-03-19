package com.moviereservationapi.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto implements Serializable {

    private Long id;

    @NotBlank(message = "Seat row field cannot be empty.")
    private String seatRow;

    @NotNull(message = "Seat number field cannot be empty.")
    private Integer seatNumber;

    @NotBlank(message = "Room field cannot be empty.")
    private String roomName;

}
