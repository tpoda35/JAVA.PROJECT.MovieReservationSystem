package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatDto;
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
    public List<SeatDetailsDtoV1> getSeats(List<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllById(seatIds);

        int seatsSize = seats.size();
        int seatIdsSize = seatIds.size();

        if (seatsSize != seatIdsSize) {
            log.info("(Feign call) Found only {} seats instead of {}.", seatsSize, seatIdsSize);
            throw new SeatNotFoundException("Didn't find all the seat.");
        }
        log.info("(Feign call) Found all seats.");

        return seats.stream()
                .map(SeatMapper::fromSeatToDetailsDtoV1)
                .toList();
    }

    @Override
    public List<SeatDto> getSeatsByRoomId(Long roomId) {
        List<Seat> seats = seatRepository.findAllByRoom_Id(roomId);
        if (seats.isEmpty()) {
            log.info("(Feign call) No seat found.");
            throw new SeatNotFoundException("There's no seat found.");
        }
        log.info("(Feign call) Found {} seats for the room with the id of {}.", seats.size(), roomId);

        return seats.stream()
                .map(SeatMapper::fromSeatToDto)
                .toList();
    }

}
