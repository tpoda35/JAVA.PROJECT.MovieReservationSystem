package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV2;

public interface IShowtimeFeignService {
    ShowtimeDetailsDtoV2 getShowtime(Long showtimeId);
    void addShowtimeReservation(Long reservationId, Long showtimeId);
}
