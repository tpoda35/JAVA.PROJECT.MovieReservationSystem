package com.moviereservationapi.payment.controller;

import com.moviereservationapi.payment.dto.ReservationPaymentRequest;
import com.moviereservationapi.payment.dto.StripeResponse;
import com.moviereservationapi.payment.service.IPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping
    public StripeResponse checkout(
            @RequestBody @Valid ReservationPaymentRequest request
    ) {
        return paymentService.checkout(request);
    }

}
