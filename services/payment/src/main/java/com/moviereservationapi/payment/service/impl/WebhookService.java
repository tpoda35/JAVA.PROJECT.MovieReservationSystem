package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.dto.payment.PaymentEvent;
import com.moviereservationapi.payment.exception.PaymentException;
import com.moviereservationapi.payment.feign.ReservationClient;
import com.moviereservationapi.payment.model.Payment;
import com.moviereservationapi.payment.repository.PaymentRepository;
import com.moviereservationapi.payment.service.IPaymentPublisher;
import com.moviereservationapi.payment.service.IWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
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

    private final PaymentRepository paymentRepository;
    private final ReservationClient reservationClient;
    private final IPaymentPublisher paymentPublisher;

    @Value("${apiKey.testSecret}")
    private String endpointSecret;

    @Override
    @Transactional
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
            log.error("(Stripe Webhook) Stripe object is null.");
            throw new PaymentException("Invalid Stripe object.");
        }

        switch (eventType) {
            case "checkout.session.completed":
            case "checkout.session.expired":
                if (!(stripeObject instanceof Session session)) {
                    log.error("(Stripe Webhook) Invalid Stripe event: expected Session for type '{}', but got {}",
                            eventType, stripeObject.getClass().getName());
                    throw new PaymentException("Expected Session object for event type " + eventType);
                }

                String reservationIdStr = session.getMetadata().get("reservationId");
                String seatIdsStr = session.getMetadata().get("seatIds");
                String showtimeIdStr = session.getMetadata().get("showtimeId");
                String userId = session.getMetadata().get("userId");
                String email = session.getMetadata().get("email");

                if (
                        reservationIdStr == null ||
                                seatIdsStr == null ||
                                showtimeIdStr == null ||
                                userId == null ||
                                email == null
                ) {
                    log.error("(Stripe Webhook) Missing metadata. reservationIdStr: {}, seatIdsStr: {}, showtimeIdStr: {}, userId: {}, email: {}.",
                            reservationIdStr, seatIdsStr, showtimeIdStr, userId, email);
                    throw new PaymentException("Missing required metadata in Stripe session.");
                }

                Long reservationId;
                List<Long> seatIds;
                long showtimeId;
                try {
                    reservationId = Long.valueOf(reservationIdStr);
                    seatIds = parseSeatIdList(seatIdsStr);
                    showtimeId = Long.parseLong(showtimeIdStr);
                } catch (NumberFormatException e) {
                    log.error("(Stripe Webhook) Invalid metadata format.");
                    throw new PaymentException("Invalid metadata format in Stripe session.");
                }

                log.info("(Stripe Webhook) Stripe event '{}' received for reservationId={}", eventType, reservationId);

                if ("checkout.session.completed".equals(eventType)) {
                    reservationClient.changeStatusToPaid(reservationId);

                    Payment payment = Payment.builder()
                            .showtimeId(showtimeId)
                            .seatIds(seatIds)
                            .userId(userId)
                            .userMail(email)
                            .build();

                    paymentRepository.save(payment);

                    log.info("(Stripe Webhook) Saved new payment: {}.", payment);

                    paymentPublisher.publishPaymentSuccess(
                            PaymentEvent.builder()
                                    .showtimeId(showtimeId)
                                    .seatIds(seatIds)
                                    .userId(userId)
                                    .email(email)
                                    .build()
                    );

                    log.info("(Stripe Webhook) Published PaymentEvent.");
                } else {
                    log.warn("(Stripe Webhook) Failed payment.");
                    reservationClient.changeStatusToFailed(reservationId);
                }
                break;

            default:
                log.info("(Stripe Webhook) Unhandled event type: {}", eventType);
                log.debug("(Stripe Webhook) Received unexpected object type: {}", stripeObject.getClass().getSimpleName());
        }
    }

    private List<Long> parseSeatIdList(String input) {
        return Arrays.stream(input.replaceAll("[\\[\\]\\s]", "").split(","))
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }

}
