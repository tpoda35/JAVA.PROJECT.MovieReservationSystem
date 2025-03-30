package com.moviereservationapi.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CinemaDetailsDtoV2 {

    private Long id;
    private String name;
    private String location;

}
