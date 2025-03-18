package com.moviereservationapi.cinema.controller;

import com.moviereservationapi.cinema.model.Cinema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cinemas")
@Slf4j
@RequiredArgsConstructor
public class CinemaController {

    @GetMapping
    public CompletableFuture<Page<Cinema>> getAllCinema(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return null;
    }

}
