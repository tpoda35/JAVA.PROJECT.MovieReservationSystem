package com.moviereservationapi.cinema.service;

public interface IRoomFeignService {
    void addShowtimeToRoom(Long showtimeId, Long roomId);
    void deleteShowtimeFromRoom(Long showtimeId, Long roomId);
}
