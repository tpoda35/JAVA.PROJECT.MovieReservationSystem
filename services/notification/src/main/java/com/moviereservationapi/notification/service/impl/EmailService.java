package com.moviereservationapi.notification.service.impl;

import com.moviereservationapi.notification.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderMail;

    @Override
    public void sendEmail(String toEmail, String subject, String htmlContent) {
        String LOG_PREFIX = "sendEmail";
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(senderMail);

            javaMailSender.send(mimeMessage);
            log.info("{} :: Email sent to email: {}, with the subject of: {}.", LOG_PREFIX, toEmail, subject);
        } catch (MessagingException e) {
            log.info("{} :: Email failed to email: {}, with the subject of: {}.", LOG_PREFIX, toEmail, subject);
            throw new RuntimeException("Failed to send email", e); // Add custom exc.
        }
    }
}
