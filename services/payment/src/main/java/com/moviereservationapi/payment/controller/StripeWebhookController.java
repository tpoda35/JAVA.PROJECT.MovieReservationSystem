package com.moviereservationapi.payment.controller;

import com.moviereservationapi.payment.service.IWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/webhooks")
@Slf4j
@RequiredArgsConstructor
public class StripeWebhookController {

    private final IWebhookService webhookService;

    @PostMapping
    public void handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        webhookService.handleStripeEvent(payload, sigHeader);
    }

}
