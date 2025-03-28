package com.moviereservationapi.reservation.feign;

import com.moviereservationapi.reservation.dto.ShowtimeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "showtime-service", url = "http://localhost:8092")
public interface ShowtimeClient {

    @GetMapping("/api/showtimes/feign/getShowtime/{showtimeId}")
    ShowtimeDto getShowtime(@PathVariable("showtimeId") Long showtimeId);

    @PostMapping("/api/showtimes/feign/addShowtimeReservation/{showtimeId}/{reservationId}")
    void addShowtimeReservation(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("reservationId") Long reservationId
    );

}
