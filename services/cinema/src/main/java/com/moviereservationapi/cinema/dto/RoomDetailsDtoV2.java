package com.moviereservationapi.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailsDtoV2 implements Serializable {

    private Long id;
    private String name;
    private Integer totalSeat;
    private List<ShowtimeDto> showtimes;
    private CinemaDetailsDtoV2 cinema;

}
