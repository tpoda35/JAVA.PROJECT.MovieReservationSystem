package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDetailsDtoV1;

public interface IShowtimeFeignService {
    ShowtimeDetailsDtoV1 getShowtime(Long showtimeId);
    void addShowtimeReservation(Long reservationId, Long showtimeId);
}
