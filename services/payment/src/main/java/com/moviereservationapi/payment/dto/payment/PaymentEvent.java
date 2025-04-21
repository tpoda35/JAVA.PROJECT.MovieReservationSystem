package com.moviereservationapi.payment.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentEvent {

    private Long id;
    private Long showtimeId;
    private List<Long> seatIds = new ArrayList<>();
    private Long userId;
    private LocalDateTime createdAt;

}
