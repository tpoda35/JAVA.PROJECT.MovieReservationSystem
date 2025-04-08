package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV2;
import com.moviereservationapi.cinema.dto.seat.SeatDto;
import com.moviereservationapi.cinema.service.ISeatFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignSeatController {

    private final ISeatFeignService seatFeignService;

    @GetMapping("/getSeats")
    public List<SeatDetailsDtoV2> getSeats(
            @RequestParam List<Long> seatIds
    ) {
        return seatFeignService.getSeats(seatIds);
    }

    @GetMapping("/getSeatsByRoomId/{roomId}")
    public List<SeatDto> getSeatsByRoomId(
            @PathVariable("roomId") Long roomId
    ) {
        return seatFeignService.getSeatsByRoomId(roomId);
    }

}
