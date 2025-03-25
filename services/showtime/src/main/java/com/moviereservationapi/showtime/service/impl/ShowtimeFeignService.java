package com.moviereservationapi.showtime.service.impl;

import com.moviereservationapi.showtime.repository.ShowtimeRepository;
import com.moviereservationapi.showtime.service.IShowtimeFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShowtimeFeignService implements IShowtimeFeignService {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public Boolean showtimeExists(Long showtimeId) {
        boolean exists = showtimeRepository.existsById(showtimeId);
        log.info("(Feign call) Showtime with the id of {} exists: {}.", showtimeId, exists);
        return exists;
    }
}
