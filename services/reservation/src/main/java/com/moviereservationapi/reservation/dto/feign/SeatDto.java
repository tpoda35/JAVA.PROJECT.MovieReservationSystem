package com.moviereservationapi.reservation.dto.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {

    private Long id;
    private String seatRow;
    private Integer seatNumber;
    private String roomName;

}
