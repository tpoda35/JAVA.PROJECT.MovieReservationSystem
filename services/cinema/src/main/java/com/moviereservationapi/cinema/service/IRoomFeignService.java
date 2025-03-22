package com.moviereservationapi.cinema.service;

public interface IRoomFeignService {
    Boolean roomExists(Long roomId);
}
