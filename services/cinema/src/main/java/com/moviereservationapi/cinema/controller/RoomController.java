package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
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

    @GetMapping("/cinema/{cinemaId}")
    public CompletableFuture<Page<RoomDetailsDtoV1>> getRoomsByCinema(
            @PathVariable("cinemaId") Long cinemaId,
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        log.info("getRoomsByCinema :: Called endpoint. (pageNum:{}, pageSize:{}, cinemaId:{})", pageNum, pageSize, cinemaId);

        return roomService.getRoomsByCinema(cinemaId, pageSize, pageNum);
    }

    @GetMapping("/{roomId}")
    public CompletableFuture<RoomDetailsDtoV1> getRoomById(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("getRoomById :: Called endpoint. (roomId: {})", roomId);

        return roomService.getRoomById(roomId);
    }

}
