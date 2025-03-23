package com.moviereservationapi.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationManageDto {

    private Long showtimeId;
    private List<Long> seatIds;
    private Long userId;

}
