package com.moviereservationapi.cinema.dto;

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
public class RoomManageDto {

    @NotBlank(message = "Name field cannot be empty.")
    private String name;

    @NotBlank(message = "Total seat field cannot be empty.")
    private Integer totalSeat;

    @NotNull(message = "CinemaID field cannot be empty.")
    private Long cinemaId;

}
