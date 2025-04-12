package com.moviereservationapi.notification.service.impl;

import com.moviereservationapi.notification.repository.NotificationDetailsRepository;
import com.moviereservationapi.notification.service.INotificationDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDetailsService implements INotificationDetailsService {

    private final NotificationDetailsRepository repository;

    @Override
    public void sendEmail(String email) {

    }
}
