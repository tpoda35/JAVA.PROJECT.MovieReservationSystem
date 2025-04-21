package com.moviereservationapi.notification.service.impl;

import com.moviereservationapi.notification.dto.payment.PaymentEvent;
import com.moviereservationapi.notification.email.HtmlEmailTemplates;
import com.moviereservationapi.notification.exception.EmailSendingException;
import com.moviereservationapi.notification.feign.ReservationClient;
import com.moviereservationapi.notification.service.IEmailService;
import com.moviereservationapi.notification.service.INotificationDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDetailsService implements INotificationDetailsService {

    private final ReservationClient reservationClient;
    private final IEmailService emailService;

    @Override
    @KafkaListener(topics = "payment-notification-topic", groupId = "notification-group")
    public void handlePaymentNotification(PaymentEvent paymentEvent) {
        String LOG_PREFIX = "handlePaymentNotification";

        String subject = "Payment Notification";
        String toEmail;

        try {
            toEmail = reservationClient.getUserEmailByUserId(paymentEvent.getUserId());
            String htmlContent = HtmlEmailTemplates.getPaymentNotificationEmailTemplate(toEmail);

            emailService.sendEmail(toEmail, subject, htmlContent);

            log.info("{} :: Successfully sent payment notification to: {}", LOG_PREFIX, toEmail);
        } catch (Exception e) {
            log.error("{} :: Failed to send payment notification for userId: {}. Error: {}", LOG_PREFIX, paymentEvent.getUserId(), e.getMessage(), e);
            throw new EmailSendingException("Payment notification handling failed.");
        }
    }
}
