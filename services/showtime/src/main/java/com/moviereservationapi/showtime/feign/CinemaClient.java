package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "cinema-service", url = "http://localhost:8091")
public interface CinemaClient {

    @PostMapping("/api/rooms/feign/addShowtimeToRoom/{showtimeId}/{roomId}")
    void addShowtimeToRoom(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("roomId") Long roomId
    );

}
