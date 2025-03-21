package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.SeatDto;
import com.moviereservationapi.cinema.dto.SeatManageDto;
import com.moviereservationapi.cinema.exception.RoomNotFoundException;
import com.moviereservationapi.cinema.exception.SeatNotFoundException;
import com.moviereservationapi.cinema.mapper.SeatMapper;
import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;
import com.moviereservationapi.cinema.repository.RoomRepository;
import com.moviereservationapi.cinema.repository.SeatRepository;
import com.moviereservationapi.cinema.service.ISeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
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

    @Override
    @Async
    public CompletableFuture<SeatDto> getSeat(Long seatId) {
        String cacheKey = String.format("seat_%d", seatId);
        Cache cache = cacheManager.getCache("seat");

        SeatDto seatDto = getCachedData(cache, cacheKey, "api/seats/seatId", SeatDto.class);
        if (seatDto != null) {
            return CompletableFuture.completedFuture(seatDto);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                seatDto = getCachedData(cache, cacheKey, "api/seats/seatId", SeatDto.class);
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
    public CompletableFuture<List<SeatDto>> getAllSeatByRoom(Long roomId) {
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

    // Cache management will be optimized (I hope)
    @Override
    @CacheEvict(
            value = "cinema_seats",
            allEntries = true
    )
    public SeatDto addSeat(@Valid SeatManageDto seatManageDto) {
        log.info("api/seats (addSeat) :: Evicting 'cinema_seats' cache. Saving new seat: {}", seatManageDto);
        final Long roomId = seatManageDto.getRoomId();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> {
                    log.info("api/seats (addSeat) :: Room not found with the id of {}.", roomId);
                    return new RoomNotFoundException("Room not found.");
                });

        Seat savedSeat = seatRepository.save(SeatMapper.fromManageDtoToSeat(seatManageDto, room));

        log.info("api/seats :: Saved seat: {}.", savedSeat);

        return SeatMapper.fromSeatToDto(savedSeat);
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
    public SeatDto editSeat(Long seatId, SeatManageDto seatManageDto) {
        log.info("api/seats/seatId (editSeat) :: Evicting cache 'seat' and 'cinema_seats' with the key of 'seat_{}'", seatId);
        log.info("api/seats/seatId (editSeat) :: Editing seat with the id of {} and data of {}", seatId, seatManageDto);


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

    private <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return type.cast(cachedResult.get());
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
