package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.dto.SeatManageDto;
import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.exception.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ICacheService;
import com.moviereservationapi.cinema.service.ISeatService;
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
    public CompletableFuture<SeatDetailsDtoV1> getSeat(Long seatId) {
        String cacheKey = String.format("seat_%d", seatId);
        Cache cache = cacheManager.getCache("seat");
        String LOG_PREFIX = "api/seats/seatId";

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

                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> {
                            log.error("{} :: Seat not found with id: {}", LOG_PREFIX, seatId);
                            return new SeatNotFoundException("Seat not found.");
                        });

                seatDetailsDtoV1 = SeatMapper.fromSeatToDto(seat);
                cacheService.saveInCache(cache, cacheKey, seatDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(seatDetailsDtoV1);
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
    @Async
    public CompletableFuture<List<SeatDetailsDtoV1>> getAllSeatByRoom(Long roomId) {
        String cacheKey = String.format("cinema_seats_%d", roomId);
        Cache cache = cacheManager.getCache("cinema_seats");
        String LOG_PREFIX = "api/seats/room/roomId";

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
                            .map(SeatMapper::fromSeatToDto)
                            .toList();
                    cacheService.saveInCache(cache, cacheKey, mappedSeats, LOG_PREFIX);

                    return mappedSeats;
                }));
            } else {
                log.warn("{}:: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
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

    // Cache management will be optimized (I hope)
    @Override
    @CacheEvict(
            value = "cinema_seats",
            allEntries = true
    )
    public SeatDetailsDtoV1 addSeat(@Valid SeatManageDto seatManageDto) {
        log.info("api/seats (addSeat) :: Evicting 'cinema_seats' cache. Saving new seat: {}", seatManageDto);
        final Long roomId = seatManageDto.getRoomId();

        return transactionTemplate.execute(status -> {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> {
                        log.info("api/seats (addSeat) :: Room not found with the id of {}.", roomId);
                        return new RoomNotFoundException("Room not found.");
                    });

            Seat savedSeat = seatRepository.save(SeatMapper.fromManageDtoToSeat(seatManageDto, room));
            room.getSeat().add(savedSeat);

            log.info("api/seats :: Saved seat: {} added to room with the id of: {}.", savedSeat, roomId);

            return SeatMapper.fromSeatToDto(savedSeat);
        });
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
    public SeatDetailsDtoV1 editSeat(Long seatId, @Valid SeatManageDto seatManageDto) {
        log.info("api/seats/seatId (editSeat) :: Evicting cache 'seat' and 'cinema_seats' with the key of 'seat_{}'", seatId);
        log.info("api/seats/seatId (editSeat) :: Editing seat with the id of {} and data of {}", seatId, seatManageDto);

        return transactionTemplate.execute(status -> {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> {
                        log.info("api/seats/seatId (editSeat) :: Seat not found with the id of {}.", seatId);
                        return new SeatNotFoundException("Seat not found.");
                    });
            log.info("api/seats/seatId (editSeat) :: Seat found with the id of {}.", seatId);

            if (seatManageDto.getRoomId() != null) {
                final Long roomId = seatManageDto.getRoomId();
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> {
                            log.info("api/seats/seatId (editSeat) :: Room not found with the id of {}.", roomId);
                            return new RoomNotFoundException("Room not found.");
                        });
                seat.setRoom(room);
            }

            seat.setSeatRow(seatManageDto.getSeatRow());
            seat.setSeatNumber(seatManageDto.getSeatNumber());

            Seat savedSeat = seatRepository.save(seat);
            log.info("api/seats/seatId (editSeat) :: Saved seat: {}", seat);

            return SeatMapper.fromSeatToDto(savedSeat);
        });
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
        log.info("api/seats/seatId (deleteSeat) :: Evicting cache 'seat' and 'cinema_seats' with the key of 'seat_{}'", seatId);
        log.info("api/seats/seatId (deleteSeat) :: Deleting seat with the id of {}.", seatId);

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> {
                    log.info("api/seats/seatId (deleteSeat) :: Seat not found with the id of {}.", seatId);
                    return new SeatNotFoundException("Seat not found.");
                });
        log.info("api/seats/seatId (deleteSeat) :: Seat found with the id of {} and data of {}.", seatId, seat);

        seatRepository.delete(seat);
    }
}
