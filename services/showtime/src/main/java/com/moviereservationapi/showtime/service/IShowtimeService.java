package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDto;
import com.moviereservationapi.showtime.dto.ShowtimeManageDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IShowtimeService {
    CompletableFuture<Page<ShowtimeDto>> getShowtimes(int pageNum, int pageSize);
    CompletableFuture<ShowtimeDto> getShowtime(Long showtimeId);
    ShowtimeDto addShowtime(ShowtimeManageDto showtimeManageDto);
}
