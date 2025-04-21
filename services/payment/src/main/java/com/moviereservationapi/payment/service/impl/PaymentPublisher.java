package com.moviereservationapi.payment.service.impl;

import com.moviereservationapi.payment.dto.payment.PaymentEvent;
import com.moviereservationapi.payment.service.IPaymentPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentPublisher implements IPaymentPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topic;

    @Override
    public void publishPaymentSuccess(PaymentEvent paymentEvent) {
        kafkaTemplate.send(topic, paymentEvent);
    }
}
