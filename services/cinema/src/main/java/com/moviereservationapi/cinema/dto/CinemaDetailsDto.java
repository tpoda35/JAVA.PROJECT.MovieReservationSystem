package com.moviereservationapi.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CinemaDetailsDto implements Serializable {

    private Long id;
    private String name;
    private String location;
    private Integer roomNum;

}
