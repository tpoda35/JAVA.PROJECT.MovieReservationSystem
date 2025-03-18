package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.service.ICinemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class CinemaService implements ICinemaService {

    @Override
    public CompletableFuture<Page<Cinema>> getAllCinema(int pageNum, int pageSize) {
        return null;
    }

}
