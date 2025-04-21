package com.moviereservationapi.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "reservation-service", url = "http://localhost:8093")
public interface ReservationClient {

    @PostMapping("/api/reservations/feign/getUserEmailByUserId/{userId}")
    String getUserEmailByUserId(
            @PathVariable("userId") Long userId
    );

}
