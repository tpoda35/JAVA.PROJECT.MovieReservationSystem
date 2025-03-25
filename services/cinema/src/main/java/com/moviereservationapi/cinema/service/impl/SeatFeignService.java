package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ISeatFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatFeignService implements ISeatFeignService {

    private final SeatRepository seatRepository;

    @Override
    public Boolean seatsExists(List<Long> seatIds) {
        long foundSeatCount = seatRepository.countByIdIn(seatIds);
        long seatCount = seatIds.size();
        log.info("(Feign call) Found {} seats and required {} seats.", foundSeatCount, seatCount);
        return foundSeatCount == seatCount;
    }

}
