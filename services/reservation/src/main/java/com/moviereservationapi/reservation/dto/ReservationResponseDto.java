package com.moviereservationapi.reservation.dto;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponseDto {

    private Long id;
    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private List<SeatDto> seatDtos;
    private ShowtimeDto showtimeDto;

}
