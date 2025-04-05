package com.moviereservationapi.showtime.service.impl;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.exception.ShowtimeNotFoundException;
import com.moviereservationapi.showtime.mapper.ShowtimeMapper;
import com.moviereservationapi.showtime.model.Showtime;
import com.moviereservationapi.showtime.repository.ShowtimeRepository;
import com.moviereservationapi.showtime.service.IShowtimeFeignService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShowtimeFeignService implements IShowtimeFeignService {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public ShowtimeDetailsDtoV1 getShowtime(Long showtimeId) {
        Showtime showtime = findShowtime(showtimeId);
        log.info("(Feign call) Showtime found the id of {}.", showtimeId);

        return ShowtimeMapper.fromShowtimeToDto(showtime);
    }

    @Override
    @Transactional
    public void addShowtimeReservation(Long reservationId, Long showtimeId) {
        Showtime showtime = findShowtime(showtimeId);
        showtime.getReservationIds().add(reservationId);
        log.info("(Feign call) Showtime found the id of {}, added a new reservation with the id of {}.", showtimeId, reservationId);

        showtimeRepository.save(showtime);
    }

    private Showtime findShowtime(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> {
                    log.info("(Feign call) Showtime with the id of {} not found.", showtimeId);
                    return new ShowtimeNotFoundException("Showtime not found.");
                });
    }
}
