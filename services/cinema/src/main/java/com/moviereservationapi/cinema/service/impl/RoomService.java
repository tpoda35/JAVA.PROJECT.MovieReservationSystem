package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomManageDtoV2;
import com.moviereservationapi.cinema.exception.CinemaNotFoundException;
import com.moviereservationapi.cinema.exception.LockAcquisitionException;
import com.moviereservationapi.cinema.exception.LockInterruptedException;
import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.mapper.RoomMapper;
import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.repository.CinemaRepository;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.service.ICacheService;
import com.moviereservationapi.cinema.service.IRoomService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
        String LOG_PREFIX = "getRoomsByCinema";

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
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Async
    public CompletableFuture<RoomDetailsDtoV1> getRoomById(Long roomId) {
        String cacheKey = String.format("room_%d", roomId);
        Cache cache = cacheManager.getCache("room");
        String LOG_PREFIX = "getRoomById";

        RoomDetailsDtoV1 roomDetailsDtoV1 =
                cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, RoomDetailsDtoV1.class);
        if (roomDetailsDtoV1 != null) {
            return CompletableFuture.completedFuture(roomDetailsDtoV1);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                roomDetailsDtoV1 =
                        cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, RoomDetailsDtoV1.class);
                if (roomDetailsDtoV1 != null) {
                    return CompletableFuture.completedFuture(roomDetailsDtoV1);
                }

                roomDetailsDtoV1 = transactionTemplate.execute(status -> {
                    Room room = findRoomById(roomId, LOG_PREFIX);
                    return RoomMapper.fromRoomToDetailsDtoV1(room);
                });

                cacheService.saveInCache(cache, cacheKey, roomDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(roomDetailsDtoV1);
            } else {
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "cinema_rooms",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "cinemas",
                            allEntries = true
                    )
            }
    )
    public RoomDetailsDtoV1 addRoom(@Valid RoomManageDtoV1 roomManageDtoV1) {
        String LOG_PREFIX = "addRoom";
        Long cinemaId = roomManageDtoV1.getCinemaId();

        log.info("{} :: Evicting 'cinema_rooms' cache. Saving new room: {}", LOG_PREFIX, roomManageDtoV1);

        Cinema cinema = findCinemaById(cinemaId, LOG_PREFIX);

        Room room = RoomMapper.fromManageDtoV1ToRoom(roomManageDtoV1, cinema);
        Room savedRoom = roomRepository.save(room);

        log.info("{} :: Saved room: {}.", LOG_PREFIX, savedRoom);

        return RoomMapper.fromRoomToDetailsDtoV1(savedRoom);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "cinema_rooms",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "room",
                            key = "'room_' + #roomId"
                    )
            }
    )
    public RoomDetailsDtoV1 editRoom(@Valid RoomManageDtoV2 roomManageDtoV2, Long roomId) {
        String LOG_PREFIX = "editRoom";

        log.info("{} :: Evicting cache 'cinema_rooms' and 'room' with the key of 'room_{}'", LOG_PREFIX,  roomId);
        log.info("{} :: Editing room with the id of {} and data of {}.", LOG_PREFIX, roomId, roomManageDtoV2);

        Room room = findRoomById(roomId, LOG_PREFIX);
        log.info("{} :: Room found with the id of {}.", LOG_PREFIX, roomId);

        room.setName(roomManageDtoV2.getName());
        room.setTotalSeat(roomManageDtoV2.getTotalSeat());

        Room savedRoom = roomRepository.save(room);
        log.info("{} :: Saved room: {}", LOG_PREFIX, savedRoom);

        return RoomMapper.fromRoomToDetailsDtoV1(room);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "cinema_rooms",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "room",
                            key = "'room_' + #roomId"
                    )
            }
    )
    public void deleteRoom(Long roomId) {
        String LOG_PREFIX = "deleteRoom";

        log.info("{} :: Evicting cache 'cinema_rooms' and 'room' with the key of 'room_{}'", LOG_PREFIX, roomId);
        log.info("{} :: Deleting room with the id of {}.", LOG_PREFIX, roomId);

        Room room = findRoomById(roomId, LOG_PREFIX);
        log.info("{} :: Room found with the id of {} and data of {}.", LOG_PREFIX, roomId, room);

        roomRepository.delete(room);
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Cinema findCinemaById(Long cinemaId, String LOG_PREFIX) {
        return cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.info("{} :: Cinema not found with the id of {}.", LOG_PREFIX, cinemaId);
                    return new CinemaNotFoundException("Cinema not found.");
                });
    }

    private Room findRoomById(Long roomId, String LOG_PREFIX) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.info("{} :: Room not found with the id of {}.", LOG_PREFIX, roomId);
                    return new RoomNotFoundException("Room not found.");
                });
    }

}
