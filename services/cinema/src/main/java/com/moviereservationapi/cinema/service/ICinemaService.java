package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV2;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface ICinemaService {
    CompletableFuture<Page<CinemaDetailsDtoV1>> getCinemas(int pageNum, int pageSize);
    CompletableFuture<CinemaDetailsDtoV1> getCinema(Long cinemaId);
    CinemaDetailsDtoV1 addCinema(CinemaManageDto cinemaManageDto);
    CinemaDetailsDtoV2 editCinema(CinemaManageDto cinemaManageDto, Long cinemaId);
    void deleteCinema(Long cinemaId);
}
