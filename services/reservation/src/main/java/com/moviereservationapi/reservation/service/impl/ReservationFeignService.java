package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.repository.ReservationRepository;
import com.moviereservationapi.reservation.service.IReservationFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationFeignService implements IReservationFeignService {

    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void deleteReservationWithShowtimeId(Long showtimeId) {
        reservationRepository.removeAllByShowtimeId(showtimeId);
    }

}
