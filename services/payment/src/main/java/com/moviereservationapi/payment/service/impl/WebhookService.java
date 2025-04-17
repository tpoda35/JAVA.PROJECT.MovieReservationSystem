package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.exception.PaymentException;
import com.moviereservationapi.payment.feign.ReservationClient;
import com.moviereservationapi.payment.model.Payment;
import com.moviereservationapi.payment.repository.PaymentRepository;
import com.moviereservationapi.payment.service.IWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService implements IWebhookService {

    @Value("${apiKey.testSecret}")
    private String endpointSecret;
    private final PaymentRepository paymentRepository;
    private final ReservationClient reservationClient;

    @Override
    public void handleStripeEvent(String payload, String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.warn("(Stripe Webhook) Invalid signature.");
            throw new PaymentException("Invalid signature.");
        }

        String eventType = event.getType();
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        if (stripeObject == null) {
            throw new PaymentException("Invalid stripe object.");
        }

        switch (eventType) {
            case "checkout.session.completed":
            case "checkout.session.expired":
                if (!(stripeObject instanceof Session session)) {
                    log.error("Expected Session but got {}", stripeObject.getClass().getName());
                    throw new PaymentException("Expected Session object for event type " + eventType);
                }

                String reservationIdStr = session.getMetadata().get("reservationId");
                String seatIdsStr = session.getMetadata().get("seatIds");
                if (reservationIdStr == null) {
                    throw new PaymentException("Missing ReservationId in session metadata.");
                }

                Long reservationId;
                List<Long> seatIds;
                try {
                    reservationId = Long.valueOf(reservationIdStr);
                    seatIds = parseSeatIdList(seatIdsStr);
                } catch (NumberFormatException e) {
                    throw new PaymentException("Invalid reservationId format.");
                }

                log.info("Event type: {}", eventType);

                if ("checkout.session.completed".equals(eventType)) {
                    reservationClient.changeStatusToPaid(reservationId);

                    Payment payment = Payment.builder()
                            .seatIds(seatIds)
                            .build();
                } else {
                    reservationClient.changeStatusToFailed(reservationId);
                }
                break;

            default:
                log.info("(Stripe Webhook) Unhandled event type: {}", eventType);
                log.debug("Received unexpected object type: {}", stripeObject.getClass().getSimpleName());
        }
    }

    private List<Long> parseSeatIdList(String input) {
        return Arrays.stream(input.replaceAll("[\\[\\]\\s]", "").split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
