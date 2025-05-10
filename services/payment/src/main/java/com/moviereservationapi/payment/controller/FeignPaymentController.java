package com.moviereservationapi.payment.controller;

import com.moviereservationapi.payment.dto.payment.StripeResponse;
import com.moviereservationapi.payment.dto.reservation.ReservationPayment;
import com.moviereservationapi.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class FeignPaymentController {

    private final IPaymentService paymentService;

    @PostMapping
    public StripeResponse checkout(
            @RequestParam(defaultValue = "EUR") String currency,
            @RequestBody ReservationPayment reservationData
            ) {
        return paymentService.checkout(reservationData, currency);
    }

}
