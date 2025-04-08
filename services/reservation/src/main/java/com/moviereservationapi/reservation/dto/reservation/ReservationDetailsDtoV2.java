package com.moviereservationapi.reservation.dto.reservation;

import com.moviereservationapi.reservation.Enum.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDetailsDtoV2 implements Serializable {

    private Long id;

    private LocalDateTime reservationTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

}
