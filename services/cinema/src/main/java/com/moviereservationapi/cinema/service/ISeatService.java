package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.SeatDto;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.CompletableFuture;

public interface ISeatService {
    public CompletableFuture<SeatDto> getSeat(Long seatId);
}
