package com.moviereservationapi.movie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CinemaDto implements Serializable {

    private Long id;

    @NotBlank(message = "Name field cannot be empty.")
    private String name;

    @NotBlank(message = "Address field cannot be empty.")
    private String address;
}
