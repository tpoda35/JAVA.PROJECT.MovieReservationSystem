package com.moviereservationapi.cinema.dto.room;

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
public class RoomManageDtoV1 {

    @NotBlank(message = "Name field cannot be empty.")
    private String name;

    @NotNull(message = "Total seat field cannot be empty.")
    private Integer totalSeat;

    @NotNull(message = "CinemaID field cannot be empty.")
    private Long cinemaId;

}
