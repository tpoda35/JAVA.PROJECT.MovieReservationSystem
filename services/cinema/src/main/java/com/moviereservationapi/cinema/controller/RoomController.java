package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.service.IRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/rooms")
@Slf4j
@RequiredArgsConstructor
public class RoomController {

    private final IRoomService roomService;

    @GetMapping("/{cinemaId}")
    public CompletableFuture<Page<RoomDetailsDtoV1>> getRoomsByCinema(
            @PathVariable("cinemaId") Long cinemaId,
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        log.info("api/rooms/cinemaId :: Called endpoint. (pageNum:{}, pageSize:{}, cinemaId:{})", pageNum, pageSize, cinemaId);

        return roomService.getRoomsByCinema(cinemaId, pageSize, pageNum);
    }

}
