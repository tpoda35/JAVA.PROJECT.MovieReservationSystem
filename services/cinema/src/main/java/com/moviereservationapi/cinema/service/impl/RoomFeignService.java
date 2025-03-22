package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.service.IRoomFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomFeignService implements IRoomFeignService {

    private final RoomRepository roomRepository;

    @Override
    public Boolean roomExists(Long roomId) {
        boolean exists = roomRepository.existsById(roomId);
        log.info("(Feign call) Room with the id of {} exists: {}.", roomId, exists);
        return exists;
    }

}
