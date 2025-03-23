package com.moviereservationapi.reservation.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "showtime-service", url = "http://localhost:8092")
public interface ShowtimeClient {
}
