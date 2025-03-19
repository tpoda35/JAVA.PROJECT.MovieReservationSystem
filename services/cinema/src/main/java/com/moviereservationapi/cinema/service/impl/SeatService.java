package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ISeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatService implements ISeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    private final TransactionTemplate transactionTemplate;

    @Override
    @Async
    public CompletableFuture<SeatDto> getSeat(Long seatId) {
        String cacheKey = String.format("seat_%d", seatId);
        Cache cache = cacheManager.getCache("seat");

        SeatDto seatDto = getCachedSeat(cache, cacheKey, "api/seats/seatId");
        if (seatDto != null) {
            return CompletableFuture.completedFuture(seatDto);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                seatDto = getCachedSeat(cache, cacheKey, "api/seats/seatId");
                if (seatDto != null) {
                    return CompletableFuture.completedFuture(seatDto);
                }

                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> {
                            log.error("api/seats/seatId :: Seat not found with id: {}", seatId);
                            return new SeatNotFoundException("Seat not found.");
                        });

                seatDto = SeatMapper.fromSeatToDto(seat);
                if (cache != null) {
                    cache.put(cacheKey, seatDto);
                    log.info("api/seats/seatId :: Cached seat data for key: {}", cacheKey);
                }

                return CompletableFuture.completedFuture(seatDto);
            } else {
                log.warn("api/seats/seatId :: Failed to acquire lock for key: {}", cacheKey);
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

    @Override
    @Async
    public CompletableFuture<List<SeatDto>> getAllSeat(Long roomId) {
        String cacheKey = String.format("cinema_seats_%d", roomId);
        Cache cache = cacheManager.getCache("cinema_seats");

        List<SeatDto> seatDtos = getCachedSeatList(cache, cacheKey, "api/seats/room/roomId");
        if (seatDtos != null && !seatDtos.isEmpty()) {
            return CompletableFuture.completedFuture(seatDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                seatDtos = getCachedSeatList(cache, cacheKey, "api/seats/room/roomId");
                if (seatDtos != null && !seatDtos.isEmpty()) {
                    return CompletableFuture.completedFuture(seatDtos);
                }

                return CompletableFuture.completedFuture(transactionTemplate.execute(status -> {
                    List<Seat> seats = roomRepository.findAllSeatsByRoomId(roomId);
                    if (seats.isEmpty()) {
                        log.info("api/seats/room/roomId :: No seat found.");
                        throw new SeatNotFoundException("No seat found.");
                    }

                    return seats.stream()
                            .map(SeatMapper::fromSeatToDto)
                            .toList();
                }));
            } else {
                log.warn("api/seats/room/roomId :: Failed to acquire lock for key: {}", cacheKey);
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

    private SeatDto getCachedSeat(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (SeatDto) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }

    private List<SeatDto> getCachedSeatList(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for list key '{}'.", logPrefix, cacheKey);
        ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for list key '{}'. Returning cache.", logPrefix, cacheKey);
            return (List<SeatDto>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for list key '{}'.", logPrefix, cacheKey);
        return null;
    }
}
