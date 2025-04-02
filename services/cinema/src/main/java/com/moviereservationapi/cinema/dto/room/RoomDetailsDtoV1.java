package com.moviereservationapi.cinema.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailsDtoV1 implements Serializable {

    private Long id;
    private String name;
    private Integer totalSeat;

}
