package com.moviereservationapi.notification.repository;

import com.moviereservationapi.notification.model.NotificationDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDetailsRepository extends MongoRepository<NotificationDetails, Long> {
}
