package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ISeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatService implements ISeatService {

    private final SeatRepository seatRepository;
    private final CacheManager cacheManager;

    private final static ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<SeatDto> getSeat(Long seatId) {
        String cacheKey = String.format("seat_%d", seatId);
        Cache cache = cacheManager.getCache("seat");

        ValueWrapper cachedResult;
        log.info("api/seats/seatId :: Checking cache (1) for key '{}'.", cacheKey);
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            log.info("api/seats/seatId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
            return CompletableFuture.completedFuture((SeatDto) cachedResult.get());
        }
        log.info("api/seats/seatId :: Cache MISS for key '{}'.", cacheKey);

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                log.info("api/seats/seatId :: Checking cache (2) for key '{}'.", cacheKey);
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    log.info("api/seats/seatId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
                    return CompletableFuture.completedFuture((SeatDto) cachedResult.get());
                }
                log.info("api/seats/seatId :: Cache MISS for key '{}'. Fetching from DB...", cacheKey);

                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> {
                            log.info("api/seats/seatId :: Seat not found with the id of {}.", seatId);
                            return new SeatNotFoundException("Seat not found.");
                        });

                log.info("api/seats/seatId :: Seat found with the id of {}. Caching data for key '{}'", seatId, cacheKey);
                SeatDto result = SeatMapper.fromSeatToDto(seat);
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
