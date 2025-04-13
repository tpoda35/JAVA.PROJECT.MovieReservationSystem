package com.moviereservationapi.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationPaymentRequest {

    private List<SeatDto> seatDtos;
    private ShowtimeDto showtimeDto;
    private String currency;

}
