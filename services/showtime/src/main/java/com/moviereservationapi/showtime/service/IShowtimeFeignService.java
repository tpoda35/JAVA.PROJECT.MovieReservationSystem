package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDto;

public interface IShowtimeFeignService {
    ShowtimeDto getShowtime(Long showtimeId);
    void addShowtimeReservation(Long reservationId, Long showtimeId);
}
