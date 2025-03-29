package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.dto.CinemaDetailsDto;
import com.moviereservationapi.cinema.dto.CinemaDto;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface ICinemaService {
    CompletableFuture<Page<CinemaDetailsDto>> getCinemas(int pageNum, int pageSize);
    CompletableFuture<CinemaDetailsDto> getCinema(Long cinemaId);
    CinemaDetailsDto addCinema(CinemaManageDto cinemaManageDto);
    CinemaDto editCinema(CinemaManageDto cinemaManageDto, Long cinemaId);
    void deleteCinema(Long cinemaId);
}
