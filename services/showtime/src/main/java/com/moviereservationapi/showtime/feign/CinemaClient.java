package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cinema-service", url = "http://localhost:8091")
public interface CinemaClient {

    @GetMapping("/api/rooms/feign/roomExists/{roomId}")
    Boolean roomExists(@PathVariable("roomId") Long roomId);

}
