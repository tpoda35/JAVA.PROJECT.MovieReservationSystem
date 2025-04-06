package com.moviereservationapi.reservation.feign;

import com.moviereservationapi.reservation.dto.feign.SeatDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cinema-service", url = "http://localhost:8091")
public interface CinemaClient {

    @GetMapping("/api/seats/feign/getSeats")
    List<SeatDto> getSeats(@RequestParam List<Long> seatIds);

}
