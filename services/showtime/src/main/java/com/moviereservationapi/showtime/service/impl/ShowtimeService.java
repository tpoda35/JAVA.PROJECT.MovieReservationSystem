package com.moviereservationapi.showtime.service.impl;

import com.moviereservationapi.showtime.dto.seat.SeatAvailabilityDto;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.dto.showtime.ShowtimeCreateDto;
import com.moviereservationapi.showtime.dto.feign.SeatDto;
import com.moviereservationapi.showtime.exception.*;
import com.moviereservationapi.showtime.feign.CinemaClient;
import com.moviereservationapi.showtime.feign.MovieClient;
import com.moviereservationapi.showtime.feign.ReservationClient;
import com.moviereservationapi.showtime.mapper.ShowtimeMapper;
import com.moviereservationapi.showtime.model.Showtime;
import com.moviereservationapi.showtime.repository.ShowtimeRepository;
import com.moviereservationapi.showtime.service.ICacheService;
import com.moviereservationapi.showtime.service.IShowtimeService;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShowtimeService implements IShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieClient movieClient;
    private final CinemaClient cinemaClient;
    private final ReservationClient reservationClient;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;
    private final ICacheService cacheService;

    @Override
    @Async
    public CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimes(int pageNum, int pageSize) {
        String LOG_PREFIX = "getShowtimes";
        String cacheKey = String.format("showtimes_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("showtimes");

        Page<ShowtimeDetailsDtoV1> showtimeDtos = cacheService.getCachedShowtimePage(cache, cacheKey, LOG_PREFIX);
        if (showtimeDtos != null) {
            return CompletableFuture.completedFuture(showtimeDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                showtimeDtos = cacheService.getCachedShowtimePage(cache, cacheKey, LOG_PREFIX);
                if (showtimeDtos != null) {
                    return CompletableFuture.completedFuture(showtimeDtos);
                }

                Page<Showtime> showtimes = showtimeRepository.findAll(PageRequest.of(pageNum, pageSize));
                checkIfIsEmpty(showtimes, LOG_PREFIX, cacheKey);

                showtimeDtos = showtimes.map(ShowtimeMapper::fromShowtimeToDto);
                cacheService.saveInCache(cache, cacheKey , showtimeDtos, LOG_PREFIX);

                return CompletableFuture.completedFuture(showtimeDtos);
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
    public CompletableFuture<ShowtimeDetailsDtoV1> getShowtime(Long showtimeId) {
        String LOG_PREFIX = "getShowtime";

        String cacheKey = String.format("showtime_%d", showtimeId);
        Cache cache = cacheManager.getCache("showtime");

        ShowtimeDetailsDtoV1 showtimeDetailsDtoV1 =
                cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, ShowtimeDetailsDtoV1.class);
        if (showtimeDetailsDtoV1 != null) {
            return CompletableFuture.completedFuture(showtimeDetailsDtoV1);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                showtimeDetailsDtoV1 =
                        cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, ShowtimeDetailsDtoV1.class);
                if (showtimeDetailsDtoV1 != null) {
                    return CompletableFuture.completedFuture(showtimeDetailsDtoV1);
                }

                Showtime showtime = findShowtimeById(showtimeId, LOG_PREFIX);
                log.info("{} :: Showtime found with the id of {}. Caching data for key '{}'", LOG_PREFIX, showtimeId, cacheKey);

                showtimeDetailsDtoV1 = ShowtimeMapper.fromShowtimeToDto(showtime);
                cacheService.saveInCache(cache, cacheKey, showtimeDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(showtimeDetailsDtoV1);
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
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "showtimes",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "showtimes_movie",
                            allEntries = true
                    )
            }
    )
    @Transactional
    public ShowtimeDetailsDtoV1 addShowtime(@Valid ShowtimeCreateDto showtimeCreateDto) {
        String LOG_PREFIX = "addShowtime";

        log.info("{} :: Evicting 'showtimes' cache. Saving new showtime: {}", LOG_PREFIX, showtimeCreateDto);
        Long movieId = showtimeCreateDto.getMovieId();
        Long roomId = showtimeCreateDto.getRoomId();

        Showtime showtime = ShowtimeMapper.fromManageDtoToShowtime(showtimeCreateDto);
        Showtime savedShowtime = showtimeRepository.save(showtime);
        log.info("{} :: Saved showtime: {}.", LOG_PREFIX, showtime);

        Long showtimeId = savedShowtime.getId();

        movieClient.addShowtimeToMovie(showtimeId, movieId);
        cinemaClient.addShowtimeToRoom(showtimeId, roomId);

        return ShowtimeMapper.fromShowtimeToDto(savedShowtime);
    }

    @Override
    @Async
    public CompletableFuture<Page<ShowtimeDetailsDtoV1>> getShowtimesByMovie(Long movieId, int pageNum, int pageSize) {
        String LOG_PREFIX = "getShowtimesByMovie";
        String cacheKey = String.format("showtimes_movie_%d_page_%d_size_%d", movieId, pageNum, pageSize);
        Cache cache = cacheManager.getCache("showtimes_movie");

        Page<ShowtimeDetailsDtoV1> showtimeDtos = cacheService.getCachedShowtimePage(cache, cacheKey, LOG_PREFIX);
        if (showtimeDtos != null) {
            return CompletableFuture.completedFuture(showtimeDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                showtimeDtos = cacheService.getCachedShowtimePage(cache, cacheKey, LOG_PREFIX);
                if (showtimeDtos != null) {
                    return CompletableFuture.completedFuture(showtimeDtos);
                }

                Page<Showtime> showtimes = showtimeRepository.findByMovieId(movieId, PageRequest.of(pageNum, pageSize));
                checkIfIsEmpty(showtimes, LOG_PREFIX, cacheKey);

                showtimeDtos = showtimes.map(ShowtimeMapper::fromShowtimeToDto);
                cacheService.saveInCache(cache, cacheKey , showtimeDtos, LOG_PREFIX);

                return CompletableFuture.completedFuture(showtimeDtos);
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

    // chatGpt, recheck the seat availability at reservation.
    //    boolean isAlreadyReserved = reservationSeatRepository
    //            .existsBySeatIdAndReservation_ShowtimeId(seatId, showtimeId);
    //if (isAlreadyReserved) {
    //        throw new SeatAlreadyReservedException("Seat is already taken for this screening.");
    //    }
    @Override
    @Async
    public CompletableFuture<List<SeatAvailabilityDto>> getSeatsByShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ShowtimeNotFoundException("Showtime not found"));
        Long roomId = showtime.getRoomId();

        List<SeatDto> seats = cinemaClient.getSeatsByRoomId(roomId);
        List<Long> reservationSeatIds = reservationClient.findReservedSeatIdsByShowtimeId(showtimeId);

        return CompletableFuture.completedFuture(seats.stream()
                .map(seat -> new SeatAvailabilityDto(
                        seat.getId(),
                        seat.getSeatRow(),
                        seat.getSeatNumber(),
                        !reservationSeatIds.contains(seat.getId())
                )).toList());
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "showtimes",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "showtimes_movie",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "showtime",
                            key = "'showtime_' + #showtimeId"
                    )
            }
    )
    public void deleteShowtime(Long showtimeId) {
        String LOG_PREFIX = "deleteShowtime";

        Showtime showtime = findShowtimeById(showtimeId, LOG_PREFIX);
        log.info("{} :: Showtime found with the id of {}.", LOG_PREFIX, showtimeId);

        Long movieId = showtime.getMovieId();
        Long roomId = showtime.getRoomId();

        movieClient.deleteShowtimeFromMovie(showtimeId, movieId);
        cinemaClient.deleteShowtimeFromRoom(showtimeId, roomId);
        reservationClient.deleteReservationWithShowtimeId(showtimeId);
        showtimeRepository.delete(showtime);
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Showtime findShowtimeById(Long showtimeId, String LOG_PREFIX) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> {
                    log.info("{} :: Showtime not found with the id of {}.", LOG_PREFIX, showtimeId);
                    return new ShowtimeNotFoundException("Showtime not found.");
                });
    }

    private void checkIfIsEmpty(Page<Showtime> showtimes, String LOG_PREFIX, String cacheKey) {
        if (showtimes.isEmpty()) {
            log.info("{} :: No showtime found.", LOG_PREFIX);
            throw new ShowtimeNotFoundException("There's no showtime found.");
        }
        log.info("{} :: Found {} showtime. Caching data for key '{}'.", LOG_PREFIX, showtimes.getTotalElements(), cacheKey);
    }
}
