package com.moviereservationapi.reservation.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "showtime-service", url = "http://localhost:8092")
public interface ShowtimeClient {

    @GetMapping("/api/showtimes/feign/showtimeExists/{showtimeId}")
    Boolean showtimeExists(@PathVariable("showtimeId") Long showtimeId);

}
