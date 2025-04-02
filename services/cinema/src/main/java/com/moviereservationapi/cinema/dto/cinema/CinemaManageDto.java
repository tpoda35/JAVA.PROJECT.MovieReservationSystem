package com.moviereservationapi.cinema.dto.cinema;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CinemaManageDto {

    @NotBlank(message = "Cinema name cannot be empty.")
    private String name;

    @NotBlank(message = "Cinema location cannot be empty.")
    private String location;

}
