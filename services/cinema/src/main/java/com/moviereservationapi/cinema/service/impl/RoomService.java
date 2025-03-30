package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.RoomDetailsDtoV2;
import com.moviereservationapi.cinema.dto.RoomManageDto;
import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.mapper.RoomMapper;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.repository.CinemaRepository;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.service.ICacheService;
import com.moviereservationapi.cinema.service.IRoomService;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService implements IRoomService {

    private final RoomRepository roomRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;
    private final ICacheService cacheService;
    private final TransactionTemplate transactionTemplate;
    private final CinemaRepository cinemaRepository;

    @Override
    @Async
    public CompletableFuture<Page<RoomDetailsDtoV1>> getRoomsByCinema(Long cinemaId, int pageSize, int pageNum) {
        String cacheKey = String.format("cinema_rooms_%d_page_%d_size_%d", cinemaId, pageNum, pageSize);
        Cache cache = cacheManager.getCache("cinema_rooms");
        String LOG_PREFIX = "api/rooms";

        Page<RoomDetailsDtoV1> roomDetailsDtoV1s = cacheService.getCachedRoomPage(cache, cacheKey, LOG_PREFIX);
        if (roomDetailsDtoV1s != null && !roomDetailsDtoV1s.isEmpty()) {
            return CompletableFuture.completedFuture(roomDetailsDtoV1s);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                roomDetailsDtoV1s = cacheService.getCachedRoomPage(cache, cacheKey, LOG_PREFIX);
                if (roomDetailsDtoV1s != null && !roomDetailsDtoV1s.isEmpty()) {
                    return CompletableFuture.completedFuture(roomDetailsDtoV1s);
                }

                Page<Room> rooms = cinemaRepository.findAllRoomsByCinemaId(cinemaId, PageRequest.of(pageNum, pageSize));
                if (rooms.isEmpty()) {
                    log.info("{} :: No room found.", LOG_PREFIX);
                    throw new RoomNotFoundException("No room found.");
                }

                log.info("{} :: Found {} rooms. Caching data for key '{}'.",
                        LOG_PREFIX, rooms.getTotalElements(), cacheKey);

                roomDetailsDtoV1s = rooms.map(RoomMapper::fromRoomToDetailsDtoV1);
                cacheService.saveInCache(cache, cacheKey, roomDetailsDtoV1s, LOG_PREFIX);

                return CompletableFuture.completedFuture(roomDetailsDtoV1s);

            } else {
                log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
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
    public CompletableFuture<RoomDetailsDtoV2> getRoom(Long roomId) {
        return null;
    }

    @Override
    public RoomDetailsDtoV1 addRoom(RoomManageDto roomManageDto) {
        return null;
    }

    @Override
    public RoomDetailsDtoV2 editRoom(RoomManageDto roomManageDto, Long roomId) {
        return null;
    }

    @Override
    public void deleteRoom(Long roomId) {

    }

}
