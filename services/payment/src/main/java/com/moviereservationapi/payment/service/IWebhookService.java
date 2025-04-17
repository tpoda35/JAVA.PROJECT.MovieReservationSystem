package com.moviereservationapi.payment.service;

public interface IWebhookService {
    void handleStripeEvent(String payload, String sigHeader);
}
