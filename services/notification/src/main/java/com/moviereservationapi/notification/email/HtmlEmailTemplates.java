package com.moviereservationapi.notification.email;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HtmlEmailTemplates {
    public static String getPaymentNotificationEmailTemplate(String name) throws IOException {
        InputStream inputStream = HtmlEmailTemplates.class.getClassLoader()
                .getResourceAsStream("templates/paymentNotificationEmailTemplate.html");

        if (inputStream == null) {
            throw new IOException("Template file not found: templates/paymentNotificationEmailTemplate.html");
        }

        String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        template = template.replace("{{name}}", name);

        return template;
    }
}
