package com.moviereservationapi.showtime.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "reservation-service", url = "http://localhost:8093")
public interface ReservationClient {

    @GetMapping("/api/reservationseats/feign/findReservedSeatIdsByShowtimeId/{showtimeId}")
    List<Long> findReservedSeatIdsByShowtimeId(
            @PathVariable("showtimeId") Long showtimeId
    );

    @DeleteMapping("/api/reservations/feign/deleteReservationWithShowtimeId/{showtimeId}")
    void deleteReservationWithShowtimeId(
            @PathVariable("showtimeId") Long showtimeId
    );

}
