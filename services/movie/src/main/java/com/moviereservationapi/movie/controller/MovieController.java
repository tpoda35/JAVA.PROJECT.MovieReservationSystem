package com.moviereservationapi.movie.controller;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.service.impl.MovieService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Slf4j
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public CompletableFuture<Page<MovieDto>> getAllMovie(
            @RequestParam(defaultValue = "0") @NonNull Integer pageNum,
            @RequestParam(defaultValue = "10") @NonNull Integer pageSize
    ){
        log.info("api/movies :: Called endpoint. (pageNum:{}, pageSize:{})", pageNum, pageSize);

        return movieService.getAllMovie(pageNum, pageSize)
                .thenApply(results -> {
                    log.info("api/movies :: Found {} movie.", results.getContent().size());
                    return results;
                });
    }

}
