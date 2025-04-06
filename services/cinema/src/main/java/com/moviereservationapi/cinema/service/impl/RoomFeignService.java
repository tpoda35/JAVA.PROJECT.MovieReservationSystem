package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.service.IRoomFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomFeignService implements IRoomFeignService {

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public void addShowtimeToRoom(Long showtimeId, Long roomId) {
        Room room = findRoomById(roomId);
        room.getShowtimeIds().add(showtimeId);
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteShowtimeFromRoom(Long showtimeId, Long roomId) {
        Room room = findRoomById(roomId);

        if (room.getShowtimeIds().remove(showtimeId)) {
            roomRepository.save(room);
        } else {
            log.info("(Feign call) Showtime(id:{}) associated with the Room(id:{}) not found.", showtimeId, roomId);
            throw new IllegalArgumentException("Showtime ID not found in Room.");
        }
    }

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Room with the id of {} not found.", roomId);
                    return new RoomNotFoundException("Room not found.");
                });
    }
}
