package com.moviereservationapi.notification.service;

import com.moviereservationapi.notification.dto.payment.PaymentEvent;

public interface INotificationDetailsService {
    void handlePaymentNotification(PaymentEvent paymentEvent);
}
