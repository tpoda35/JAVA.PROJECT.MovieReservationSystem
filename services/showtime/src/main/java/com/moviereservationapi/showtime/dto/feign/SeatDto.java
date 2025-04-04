package com.moviereservationapi.showtime.dto.feign;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto implements Serializable {

    private Long id;
    private String seatRow;
    private Integer seatNumber;

}
