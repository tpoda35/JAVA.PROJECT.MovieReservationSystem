package com.moviereservationapi.notification.service;

public interface IEmailService {
    void sendEmail(String toEmail, String subject, String htmlContent);
}
