package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.seat.SeatCreateDto;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatEditDto;
import com.moviereservationapi.cinema.exception.LockAcquisitionException;
import com.moviereservationapi.cinema.exception.LockInterruptedException;
import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.exception.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ICacheService;
import com.moviereservationapi.cinema.service.ISeatService;
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
    private final ICacheService cacheService;

    @Override
    @Async
    public CompletableFuture<SeatDetailsDtoV1> getSeatById(Long seatId) {
        String cacheKey = String.format("seat_%d", seatId);
        Cache cache = cacheManager.getCache("seat");
        String LOG_PREFIX = "getSeatById";

        SeatDetailsDtoV1 seatDetailsDtoV1 = cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, SeatDetailsDtoV1.class);
        if (seatDetailsDtoV1 != null) {
            return CompletableFuture.completedFuture(seatDetailsDtoV1);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                seatDetailsDtoV1 = cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, SeatDetailsDtoV1.class);
                if (seatDetailsDtoV1 != null) {
                    return CompletableFuture.completedFuture(seatDetailsDtoV1);
                }

                Seat seat = findSeatById(seatId, LOG_PREFIX);

                seatDetailsDtoV1 = SeatMapper.fromSeatToDetailsDtoV1(seat);
                cacheService.saveInCache(cache, cacheKey, seatDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(seatDetailsDtoV1);
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
    public CompletableFuture<List<SeatDetailsDtoV1>> getAllSeatByRoom(Long roomId) {
        String cacheKey = String.format("cinema_seats_%d", roomId);
        Cache cache = cacheManager.getCache("cinema_seats");
        String LOG_PREFIX = "getAllSeatByRoom";

        List<SeatDetailsDtoV1> seatDetailsDtoV1s = cacheService.getCachedSeatList(cache, cacheKey, LOG_PREFIX);
        if (seatDetailsDtoV1s != null && !seatDetailsDtoV1s.isEmpty()) {
            return CompletableFuture.completedFuture(seatDetailsDtoV1s);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                seatDetailsDtoV1s = cacheService.getCachedSeatList(cache, cacheKey, LOG_PREFIX);
                if (seatDetailsDtoV1s != null && !seatDetailsDtoV1s.isEmpty()) {
                    return CompletableFuture.completedFuture(seatDetailsDtoV1s);
                }

                return CompletableFuture.completedFuture(transactionTemplate.execute(status -> {
                    // Maybe I should check the room first for more accurate error message.
                    List<Seat> seats = roomRepository.findAllSeatsByRoomId(roomId);
                    if (seats.isEmpty()) {
                        log.info("{} :: No seat found.", LOG_PREFIX);
                        throw new SeatNotFoundException("No seat found.");
                    }

                    var mappedSeats = seats.stream()
                            .map(SeatMapper::fromSeatToDetailsDtoV1)
                            .toList();
                    cacheService.saveInCache(cache, cacheKey, mappedSeats, LOG_PREFIX);

                    return mappedSeats;
                }));
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
    @CacheEvict(
            value = "cinema_seats",
            allEntries = true
    )
    public SeatDetailsDtoV1 addSeat(@Valid SeatCreateDto seatCreateDto) {
        String LOG_PREFIX = "addSeat";

        log.info("{} :: Evicting 'cinema_seats' cache. Saving new seat: {}", LOG_PREFIX, seatCreateDto);
        final Long roomId = seatCreateDto.getRoomId();

        Room room = findRoomById(roomId, LOG_PREFIX);

        Seat seat = SeatMapper.fromCreateDtoToSeat(seatCreateDto, room);
        Seat savedSeat = seatRepository.save(seat);
        room.getSeat().add(savedSeat);

        log.info("{} :: Saved seat: {} added to room with the id of: {}.", LOG_PREFIX, savedSeat, roomId);

        return SeatMapper.fromSeatToDetailsDtoV1(savedSeat);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "seat",
                            key = "'seat_' + #seatId"
                    ),
                    @CacheEvict(
                            value = "cinema_seats",
                            allEntries = true
                    )
            }
    )
    public SeatDetailsDtoV1 editSeat(Long seatId, @Valid SeatEditDto seatEditDto) {
        String LOG_PREFIX = "editSeat";

        log.info("{} :: Evicting cache 'seat' and 'cinema_seats' with the key of 'seat_{}'", LOG_PREFIX, seatId);
        log.info("{} :: Editing seat with the id of {} and data of {}", LOG_PREFIX, seatId, seatEditDto);

        Seat seat = findSeatById(seatId, LOG_PREFIX);
        log.info("{} :: Seat found with the id of {}.", LOG_PREFIX, seatId);

        seat.setSeatRow(seatEditDto.getSeatRow());
        seat.setSeatNumber(seatEditDto.getSeatNumber());

        Seat savedSeat = seatRepository.save(seat);
        log.info("{} :: Saved seat: {}", LOG_PREFIX, seat);

        return SeatMapper.fromSeatToDetailsDtoV1(savedSeat);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "seat",
                            key = "'seat_' + #seatId"
                    ),
                    @CacheEvict(
                            value = "cinema_seats",
                            allEntries = true
                    )
            }
    )
    public void deleteSeat(Long seatId) {
        String LOG_PREFIX = "deleteSeat";

        log.info("{} :: Evicting cache 'seat' and 'cinema_seats' with the key of 'seat_{}'", LOG_PREFIX, seatId);
        log.info("{} :: Deleting seat with the id of {}.", LOG_PREFIX, seatId);

        Seat seat = findSeatById(seatId, LOG_PREFIX);
        log.info("{} :: Seat found with the id of {} and data of {}.", LOG_PREFIX, seatId, seat);

        seatRepository.delete(seat);
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Seat findSeatById(Long seatId, String LOG_PREFIX) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> {
                    log.error("{} :: Seat not found with id: {}", LOG_PREFIX, seatId);
                    return new SeatNotFoundException("Seat not found.");
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
