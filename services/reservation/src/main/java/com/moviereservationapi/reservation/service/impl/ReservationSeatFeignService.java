package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.repository.ReservationSeatRepository;
import com.moviereservationapi.reservation.service.IReservationSeatFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationSeatFeignService implements IReservationSeatFeignService {

    private final ReservationSeatRepository reservationSeatRepository;

    @Override
    public List<Long> findReservedSeatIdsByShowtimeId(Long showtimeId) {
        return reservationSeatRepository.findReservedSeatIdsByShowtimeId(showtimeId);
    }
}
