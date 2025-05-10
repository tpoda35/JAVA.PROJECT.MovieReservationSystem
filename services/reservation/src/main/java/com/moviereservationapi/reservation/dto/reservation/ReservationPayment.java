package com.moviereservationapi.reservation.dto.reservation;

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
public class ReservationPayment {

    private Long reservationId;
    private List<Long> seatIds = new ArrayList<>();
    private Long showtimeId;
    private String userId;

}
