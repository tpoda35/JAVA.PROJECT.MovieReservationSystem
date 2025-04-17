package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.dto.payment.StripeResponse;
import com.moviereservationapi.payment.exception.PaymentException;
import com.moviereservationapi.payment.feign.ReservationClient;
import com.moviereservationapi.payment.service.IPaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PaymentService implements IPaymentService {

    private final String stripeKey;
    private final String defaultCurrency;
    private final long unitAmount;
    private final String successUrl;
    private final String cancelUrl;

    private final ReservationClient reservationClient;

    public PaymentService(
            ReservationClient reservationClient,
            @Value("${apiKey.stripeKey}") String stripeKey,
            @Value("${payment.default_currency}") String defaultCurrency,
            @Value("${payment.unit_amount}") long unitAmount,
            @Value("${payment.success_url}") String successUrl,
            @Value("${payment.cancel_url}") String cancelUrl
    ) {
        this.reservationClient = reservationClient;
        this.stripeKey = stripeKey;
        this.defaultCurrency = defaultCurrency;
        this.unitAmount = unitAmount;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeKey;
    }

    @Override
    @Transactional
    public StripeResponse checkout(Long reservationId, String currency) {
        String LOG_PREFIX = "checkout";

        // Change this to a class and get the showtimeId also to save it in the webhookservice.
        List<Long> seatIds = reservationClient.findSeatIdsByReservationId(reservationId);
        long seatNum = seatIds.size();

        if (currency == null) {
            currency = defaultCurrency;
        }

        SessionCreateParams.LineItem lineItem = new SessionCreateParams.LineItem.Builder()
                .setPriceData(
                        new SessionCreateParams.LineItem.PriceData.Builder()
                                .setCurrency(currency)
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        new SessionCreateParams.LineItem.PriceData.ProductData.Builder()
                                                .setName("Movie Seat Reservation")
                                                .build()
                                )
                                .build()
                )
                .setQuantity(seatNum)
                .build();


        reservationClient.changeStatusToUnder_Payment(reservationId);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .putMetadata("reservationId", String.valueOf(reservationId))
                .putMetadata("seatIds", String.valueOf(seatIds))
                .build();

        Session session;

        try {
            session = Session.create(params);
        } catch (StripeException exception) {
            log.info("{} :: An exception threw while creating session: {}.", LOG_PREFIX, exception.getMessage());
            throw new PaymentException(exception.getMessage(), exception);
        }

        log.info("{} :: Creating payment session for the reservationId of {}.", LOG_PREFIX, reservationId);
        return StripeResponse.builder()
                .status("SUCCESS")
                .message("Payment session created.")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
}
