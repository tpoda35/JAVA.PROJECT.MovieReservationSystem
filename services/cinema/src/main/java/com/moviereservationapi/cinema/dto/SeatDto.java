package com.moviereservationapi.cinema.dto;

import com.moviereservationapi.cinema.model.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {

    private String seatRow;
    private Integer seatNumber;
    private Room room;

}
