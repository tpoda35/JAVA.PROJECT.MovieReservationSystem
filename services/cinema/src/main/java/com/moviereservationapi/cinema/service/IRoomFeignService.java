package com.moviereservationapi.cinema.service;

public interface IRoomFeignService {
    void addShowtimeToRoom(Long showtimeId, Long roomId);
}
