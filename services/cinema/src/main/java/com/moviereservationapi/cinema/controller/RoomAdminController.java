package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV2;
import com.moviereservationapi.cinema.service.IRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/rooms")
@Slf4j
@RequiredArgsConstructor
public class RoomAdminController {

    private final IRoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDetailsDtoV1> addRoom(
            @RequestBody @Valid RoomManageDtoV1 roomManageDtoV1
    ) {
        log.info("addRoom :: Called endpoint. (RoomManageDtoV1: {})", roomManageDtoV1);

        RoomDetailsDtoV1 savedRoom = roomService.addRoom(roomManageDtoV1);
        URI location = URI.create("/rooms/" + savedRoom.getId());

        return ResponseEntity.created(location).body(savedRoom);
    }

    @PutMapping("/{roomId}")
    public RoomDetailsDtoV1 editRoom(
            @PathVariable("roomId") Long roomId,
            @RequestBody @Valid RoomManageDtoV2 roomManageDtoV2
    ) {
        log.info("editRoom :: Called endpoint. (RoomManageDtoV2: {}, roomId: {})", roomManageDtoV2, roomId);

        return roomService.editRoom(roomManageDtoV2, roomId);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable("roomId") Long roomId
    ) {
        log.info("deleteRoom :: Called endpoint. (roomId:{})", roomId);
        roomService.deleteRoom(roomId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
