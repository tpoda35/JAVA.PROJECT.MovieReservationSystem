package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.CinemaDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface ICinemaService {
    CompletableFuture<Page<CinemaDto>> getAllCinema(int pageNum, int pageSize);
    CompletableFuture<CinemaDto> getCinema(Long cinemaId);
}
