package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.dto.ShowtimeCreateDto;
import com.moviereservationapi.showtime.dto.feign.SeatDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IShowtimeService {
    CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimes(int pageNum, int pageSize);
    CompletableFuture<ShowtimeDetailsDtoV1> getShowtime(Long showtimeId);
    ShowtimeDetailsDtoV1 addShowtime(ShowtimeCreateDto showtimeCreateDto);
    CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimesByMovie(Long movieId, int pageNum, int pageSize);
    CompletableFuture<List<SeatDto>> getSeatsByShowtime(Long showtimeId);
    void deleteShowtime(Long showtimeId);
}
