package com.moviereservationapi.payment.controller;

import com.moviereservationapi.payment.dto.payment.StripeResponse;
import com.moviereservationapi.payment.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/{reservationId}")
    public StripeResponse checkout(
        @PathVariable("reservationId") Long reservationId,
        @RequestParam(defaultValue = "EUR") String currency
    ) {
        return paymentService.checkout(reservationId, currency);
    }

}
