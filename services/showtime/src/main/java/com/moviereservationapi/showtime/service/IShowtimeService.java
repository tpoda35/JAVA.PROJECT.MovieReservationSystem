package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface IShowtimeService {
    CompletableFuture<Page<ShowtimeDto>> getShowtimes(int pageNum, int pageSize);
}
