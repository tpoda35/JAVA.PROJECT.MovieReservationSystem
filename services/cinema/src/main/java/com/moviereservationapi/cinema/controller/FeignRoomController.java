package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.service.IRoomFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignRoomController {

    private final IRoomFeignService roomFeignService;

    @PostMapping("/addShowtimeToRoom/{showtimeId}/{roomId}")
    public void addShowtimeToRoom(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("roomId") Long roomId
    ) {
        log.info("(Feign call) Adding showtime to the room with the id of {}.", roomId);

        roomFeignService.addShowtimeToRoom(showtimeId, roomId);
    }

    @DeleteMapping("/deleteShowtimeFromRoom/{showtimeId}/{roomId}")
    public void deleteShowtimeFromRoom(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("roomId") Long roomId
    ) {
        log.info("(Feign call) Removing showtime with the id of {} from the room with the id of {}.", showtimeId, roomId);

        roomFeignService.deleteShowtimeFromRoom(showtimeId, roomId);
    }

}
