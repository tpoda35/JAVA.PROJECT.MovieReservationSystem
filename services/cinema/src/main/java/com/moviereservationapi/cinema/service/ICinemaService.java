package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.model.Cinema;
import org.springframework.data.domain.Page;

import java.util.concurrent.CompletableFuture;

public interface ICinemaService {

    CompletableFuture<Page<Cinema>> getAllCinema(int pageNum, int pageSize);

}
