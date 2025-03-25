package com.moviereservationapi.reservation.dto;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import com.moviereservationapi.reservation.model.ReservationSeat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private List<ReservationSeat> reservationSeats;

}
