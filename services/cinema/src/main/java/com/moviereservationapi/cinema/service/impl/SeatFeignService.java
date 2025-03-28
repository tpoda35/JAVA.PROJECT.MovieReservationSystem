package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.exception.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Seat;
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
    public List<SeatDto> getSeats(List<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllById(seatIds);

        int seatsSize = seats.size();
        int seatIdsSize = seatIds.size();

        if (seatsSize != seatIdsSize) {
            log.info("(Feign call) Found only {} seats instead of {}.", seatsSize, seatIdsSize);
            throw new SeatNotFoundException("Seat not found.");
        }
        log.info("(Feign call) Found all seats.");

        return seats.stream()
                .map(SeatMapper::fromSeatToDto)
                .toList();
    }

}
