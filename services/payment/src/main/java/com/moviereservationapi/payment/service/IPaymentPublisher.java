package com.moviereservationapi.payment.service;

import com.moviereservationapi.payment.dto.payment.PaymentEvent;

public interface IPaymentPublisher {
    void publishPaymentSuccess(PaymentEvent paymentEvent);
}
