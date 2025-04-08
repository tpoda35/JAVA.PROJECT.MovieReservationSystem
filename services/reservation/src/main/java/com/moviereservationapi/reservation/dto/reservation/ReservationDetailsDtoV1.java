package com.moviereservationapi.reservation.dto.reservation;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import com.moviereservationapi.reservation.dto.feign.SeatDto;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDetailsDtoV1 implements Serializable {

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private List<SeatDto> seatDtos;

}
