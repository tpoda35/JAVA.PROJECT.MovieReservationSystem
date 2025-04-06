package com.moviereservationapi.showtime.feign;

import com.moviereservationapi.showtime.dto.feign.SeatDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "cinema-service", url = "http://localhost:8091")
public interface CinemaClient {

    @PostMapping("/api/rooms/feign/addShowtimeToRoom/{showtimeId}/{roomId}")
    void addShowtimeToRoom(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("roomId") Long roomId
    );

    @GetMapping("/api/seats/feign/getSeatsByRoomId/{roomId}")
    List<SeatDto> getSeatsByRoomId(
            @PathVariable("roomId") Long roomId
    );

    @DeleteMapping("/api/rooms/feign/deleteShowtimeFromRoom/{showtimeId}/{roomId}")
    void deleteShowtimeFromRoom(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("roomId") Long roomId
    );

}
