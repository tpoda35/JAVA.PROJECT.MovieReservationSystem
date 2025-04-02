package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.service.ISeatFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seats/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignSeatController {

    private final ISeatFeignService seatFeignService;

    @GetMapping("/getSeats")
    public List<SeatDetailsDtoV1> getSeats(
            @RequestParam List<Long> seatIds
    ) {
        return seatFeignService.getSeats(seatIds);
    }

}
