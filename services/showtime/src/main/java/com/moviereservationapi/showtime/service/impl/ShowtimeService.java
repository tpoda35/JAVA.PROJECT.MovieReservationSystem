package com.moviereservationapi.showtime.service.impl;

import com.moviereservationapi.showtime.dto.ShowtimeDto;
import com.moviereservationapi.showtime.exception.ShowtimeNotFoundException;
import com.moviereservationapi.showtime.mapper.ShowtimeMapper;
import com.moviereservationapi.showtime.model.Showtime;
import com.moviereservationapi.showtime.repository.ShowtimeRepository;
import com.moviereservationapi.showtime.service.IShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShowtimeService implements IShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    @Override
    @Async
    public CompletableFuture<Page<ShowtimeDto>> getShowtimes(int pageNum, int pageSize) {
        String cacheKey = String.format("showtimes_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("showtimes");

        Page<ShowtimeDto> showtimeDtos = getCachedShowtimePage(cache, cacheKey, "api/showtimes");
        if (showtimeDtos != null) {
            return CompletableFuture.completedFuture(showtimeDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                showtimeDtos = getCachedShowtimePage(cache, cacheKey, "api/showtimes");
                if (showtimeDtos != null) {
                    return CompletableFuture.completedFuture(showtimeDtos);
                }

                Page<Showtime> showtimes = showtimeRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (showtimes.isEmpty()) {
                    log.info("api/showtimes :: No showtime found.");
                    throw new ShowtimeNotFoundException("There's no showtime found.");
                }

                log.info("api/showtimes :: Found {} showtime. Caching data for key '{}'.", showtimes.getTotalElements(), cacheKey);
                Page<ShowtimeDto> results = showtimes.map(ShowtimeMapper::fromShowtimeToDto);
                if (cache != null) {
                    cache.put(cacheKey, results);
                }

                return CompletableFuture.completedFuture(results);
            } else {
                log.warn("api/showtimes :: Failed to acquire lock for key: {}", cacheKey);
                throw new RuntimeException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Page<ShowtimeDto> getCachedShowtimePage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<ShowtimeDto>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }

}
