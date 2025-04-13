package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.dto.ReservationPaymentRequest;
import com.moviereservationapi.payment.dto.SeatDto;
import com.moviereservationapi.payment.dto.StripeResponse;
import com.moviereservationapi.payment.exception.PaymentException;
import com.moviereservationapi.payment.exception.SeatNotFoundException;
import com.moviereservationapi.payment.repository.PaymentRepository;
import com.moviereservationapi.payment.service.IPaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService implements IPaymentService {

    private final String stripeKey;
    private final String defaultCurrency;
    private final long unitAmount;
    private final String successUrl;
    private final String cancelUrl;

    private final PaymentRepository paymentRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            @Value("${apiKey.stripeKey}") String stripeKey,
            @Value("${payment.default_currency}") String defaultCurrency,
            @Value("${payment.unit_amount}") long unitAmount,
            @Value("${payment.success_url}") String successUrl,
            @Value("${payment.cancel_url}") String cancelUrl
    ) {
        this.paymentRepository = paymentRepository;
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
    public StripeResponse checkout(@Valid ReservationPaymentRequest request) {
        List<SeatDto> seats = request.getSeatDtos();
        if (seats == null || seats.isEmpty()) {
            throw new SeatNotFoundException("Seat(s) not found.");
        }
        long seatNum = seats.size();
        String currency = Optional.ofNullable(request.getCurrency()).orElse(defaultCurrency);
        LocalDateTime showtimeStartDate = request.getShowtimeDto().getStartTime();

        SessionCreateParams.LineItem lineItem = new SessionCreateParams.LineItem.Builder()
                .setPriceData(
                        new SessionCreateParams.LineItem.PriceData.Builder()
                                .setCurrency(currency)
                                .setUnitAmount(unitAmount) // $10.00 per seat
                                .setProductData(
                                        new SessionCreateParams.LineItem.PriceData.ProductData.Builder()
                                                .setName("Movie Seat Reservation")
                                                .putMetadata("showtime", showtimeStartDate.toString())
                                                .putMetadata("seats", String.valueOf(seatNum))
                                                .build()
                                )
                                .build()
                )
                .setQuantity(seatNum)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .build();

        Session session = null;

        try {
            session = Session.create(params);
        } catch (StripeException exception) {
            // log
            throw new PaymentException(exception.getMessage() , exception);
        }

        return StripeResponse.builder()
                .status("SUCCESS")
                .message("Payment session created.")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
    // If success -> database save, else only just a message that it is failed and delete the reservation (maybe).
}
