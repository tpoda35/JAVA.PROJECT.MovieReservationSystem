package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.dto.payment.StripeResponse;
import com.moviereservationapi.payment.dto.reservation.ReservationPayment;
import com.moviereservationapi.payment.exception.PaymentException;
import com.moviereservationapi.payment.service.IJwtService;
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

    private final IJwtService jwtService;

    public PaymentService(
            IJwtService jwtService,
            @Value("${apiKey.stripeKey}") String stripeKey,
            @Value("${payment.default_currency}") String defaultCurrency,
            @Value("${payment.unit_amount}") long unitAmount,
            @Value("${payment.success_url}") String successUrl,
            @Value("${payment.cancel_url}") String cancelUrl
    ) {
        this.jwtService = jwtService;
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
    public StripeResponse checkout(ReservationPayment reservationData, String currency) {
        String LOG_PREFIX = "checkout";

        Long reservationId = reservationData.getReservationId();
        Long showtimeId = reservationData.getShowtimeId();
        List<Long> seatIds = reservationData.getSeatIds();
        String userId = reservationData.getUserId();
        String email = jwtService.getLoggedInUserEmailFromJwt();
        long seatCount = seatIds.size();

        String usedCurrency = (currency != null) ? currency : defaultCurrency;

        SessionCreateParams.LineItem lineItem = new SessionCreateParams.LineItem.Builder()
                .setPriceData(
                        new SessionCreateParams.LineItem.PriceData.Builder()
                                .setCurrency(usedCurrency)
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        new SessionCreateParams.LineItem.PriceData.ProductData.Builder()
                                                .setName("Movie Seat Reservation")
                                                .build()
                                )
                                .build()
                )
                .setQuantity(seatCount)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setExpiresAt((System.currentTimeMillis() / 1000L) + 30 * 60)
                .addLineItem(lineItem)
                .putMetadata("reservationId", String.valueOf(reservationId))
                .putMetadata("seatIds", String.valueOf(seatIds))
                .putMetadata("showtimeId", String.valueOf(showtimeId))
                .putMetadata("userId", String.valueOf(userId))
                .putMetadata("email", email)
                .build();

        Session session;

        try {
            session = Session.create(params);
        } catch (StripeException exception) {
            log.error("{} :: Exception occurred while creating payment session: {}", LOG_PREFIX, exception.getMessage(), exception);
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
