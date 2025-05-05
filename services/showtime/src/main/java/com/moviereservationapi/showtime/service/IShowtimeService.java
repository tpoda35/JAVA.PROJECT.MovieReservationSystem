package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.seat.SeatAvailabilityDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeCreateDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IShowtimeService {
    CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimes(int pageNum, int pageSize, LocalDateTime startTime);
    CompletableFuture<ShowtimeDetailsDtoV1> getShowtime(Long showtimeId);
    ShowtimeDetailsDtoV1 addShowtime(ShowtimeCreateDto showtimeCreateDto);
    CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimesByMovie(Long movieId, int pageNum, int pageSize);
    CompletableFuture<List<SeatAvailabilityDto>> getSeatsByShowtime(Long showtimeId);
    void deleteShowtime(Long showtimeId);
}
