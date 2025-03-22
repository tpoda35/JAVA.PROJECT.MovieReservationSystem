package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.service.IRoomFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/feign")
@RequiredArgsConstructor
@Slf4j
public class FeignRoomController {

    private final IRoomFeignService roomFeignService;

    @GetMapping("/roomExists/{roomId}")
    public Boolean roomExists(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("(Feign call) Checking room existence with the id of {}.", roomId);

        return roomFeignService.roomExists(roomId);
    }

}
