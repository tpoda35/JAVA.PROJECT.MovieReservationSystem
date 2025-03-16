package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.dto.CinemaDto;
import com.moviereservationapi.movie.exception.CinemaNotFoundException;
import com.moviereservationapi.movie.mapper.CinemaMapper;
import com.moviereservationapi.movie.model.Cinema;
import com.moviereservationapi.movie.repository.CinemaRepository;
import com.moviereservationapi.movie.service.ICinemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CinemaService implements ICinemaService {

    private final CinemaRepository cinemaRepository;
    private final CacheManager cacheManager;
    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<Page<CinemaDto>> getAllCinema(int pageNum, int pageSize) {
        String cacheKey = String.format("movies_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("cinemas");

        ValueWrapper cachedResult;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
            return CompletableFuture.completedFuture(
                    (Page<CinemaDto>) cachedResult.get()
            );
        }

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
                    return CompletableFuture.completedFuture(
                            (Page<CinemaDto>) cachedResult.get()
                    );
                }

                Page<Cinema> cinemas = cinemaRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (cinemas.isEmpty()) {
                    throw new CinemaNotFoundException("There's no cinema found.");
                }

                Page<CinemaDto> results = cinemas.map(CinemaMapper::fromCinemaToDto);
                if (cache != null) {
                    cache.put(cacheKey, results);
                }
                return CompletableFuture.completedFuture(results);
            }
            finally {
                locks.remove(cacheKey, lock);
            }
        }
    }

    @Override
    @Async
    public CompletableFuture<CinemaDto> getCinema(Long cinemaId) {
        String cacheKey = String.format("movie_%d", cinemaId);
        Cache cache = cacheManager.getCache("cinema");

        ValueWrapper cachedResult;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
            return CompletableFuture.completedFuture(
                    (CinemaDto) cachedResult.get()
            );
        }

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
                    return CompletableFuture.completedFuture(
                            (CinemaDto) cachedResult.get()
                    );
                }

                Cinema cinema = cinemaRepository.findById(cinemaId)
                        .orElseThrow(() -> new CinemaNotFoundException("There's no cinema found."));

                CinemaDto result = CinemaMapper.fromCinemaToDto(cinema);
                if (cache != null) {
                    cache.put(cacheKey, result);
                }
                return CompletableFuture.completedFuture(result);
            }
            finally {
                locks.remove(cacheKey, lock);
            }
        }
    }

}
