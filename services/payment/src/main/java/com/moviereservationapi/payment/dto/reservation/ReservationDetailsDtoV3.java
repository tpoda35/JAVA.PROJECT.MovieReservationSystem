package com.moviereservationapi.payment.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDetailsDtoV3 {

    private List<Long> seatIds = new ArrayList<>();
    private Long showtimeId;

}
