package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.dto.CinemaDto;
import com.moviereservationapi.movie.dto.CinemaManageDto;
import com.moviereservationapi.movie.exception.CinemaNotFoundException;
import com.moviereservationapi.movie.mapper.CinemaMapper;
import com.moviereservationapi.movie.mapper.MovieMapper;
import com.moviereservationapi.movie.model.Cinema;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.CinemaRepository;
import com.moviereservationapi.movie.service.ICinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
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
        String cacheKey = String.format("cinemas_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("cinemas");

        ValueWrapper cachedResult;
        log.info("api/cinemas :: Checking cache (1) for key '{}'.", cacheKey);
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
            log.info("api/cinemas :: Cache HIT for key '{}'. Returning cache.", cacheKey);
            return CompletableFuture.completedFuture(
                    (Page<CinemaDto>) cachedResult.get()
            );
        }
        log.info("api/cinemas :: Cache MISS for key '{}'.", cacheKey);

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                log.info("api/cinemas :: Checking cache (2) for key '{}'.", cacheKey);
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
                    log.info("api/cinemas :: Cache HIT for key '{}'. Returning cache.", cacheKey);
                    return CompletableFuture.completedFuture(
                            (Page<CinemaDto>) cachedResult.get()
                    );
                }
                log.info("api/cinemas :: Cache MISS for key '{}'. Fetching from DB...", cacheKey);

                Page<Cinema> cinemas = cinemaRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (cinemas.isEmpty()) {
                    log.info("api/cinemas :: No cinemas found.");
                    throw new CinemaNotFoundException("There's no cinema found.");
                }

                log.info("api/cinemas :: Found {} cinemas. Caching data for key '{}'.", cinemas.getTotalElements(), cacheKey);
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
        String cacheKey = String.format("cinema_%d", cinemaId);
        Cache cache = cacheManager.getCache("cinema");

        ValueWrapper cachedResult;
        log.info("api/cinemas/cinemaId :: Checking cache (1) for key '{}'.", cacheKey);
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
            log.info("api/cinemas/cinemaId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
            return CompletableFuture.completedFuture(
                    (CinemaDto) cachedResult.get()
            );
        }
        log.info("api/cinemas/cinemaId :: Cache MISS for key '{}'.", cacheKey);

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                log.info("api/cinemas/cinemaId :: Checking cache (2) for key '{}'.", cacheKey);
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null){
                    log.info("api/cinemas/cinemaId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
                    return CompletableFuture.completedFuture(
                            (CinemaDto) cachedResult.get()
                    );
                }
                log.info("api/cinemas/cinemaId :: Cache MISS for key '{}'. Fetching from DB...", cacheKey);

                Cinema cinema = cinemaRepository.findById(cinemaId)
                        .orElseThrow(() -> {
                            log.info("api/cinemas/cinemaId :: Cinema not found with the id of {}.", cinemaId);
                            return new CinemaNotFoundException("There's no cinema found.");
                        });

                log.info("api/cinemas/cinemaId :: Cinema found with the id of {}. Caching data for key '{}'", cinemaId, cacheKey);
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

    @Override
    public CinemaDto addCinema(Long cinemaId, @Valid CinemaManageDto cinemaManageDto) {

        return null;
    }


}
