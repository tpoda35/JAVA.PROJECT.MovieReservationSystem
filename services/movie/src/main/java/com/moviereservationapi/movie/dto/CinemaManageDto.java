package com.moviereservationapi.movie.dto;

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

    @NotBlank(message = "Name field cannot be empty.")
    private String name;

    @NotBlank(message = "Address field cannot be empty.")
    private String address;

}
