package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.cinema.CinemaManageDto;
import com.moviereservationapi.cinema.exception.CinemaNotFoundException;
import com.moviereservationapi.cinema.exception.LockAcquisitionException;
import com.moviereservationapi.cinema.exception.LockInterruptedException;
import com.moviereservationapi.cinema.mapper.CinemaMapper;
import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.repository.CinemaRepository;
import com.moviereservationapi.cinema.service.ICacheService;
import com.moviereservationapi.cinema.service.ICinemaService;
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
public class CinemaService implements ICinemaService {

    private final CinemaRepository cinemaRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;
    private final ICacheService cacheService;

    @Override
    @Async
    public CompletableFuture<Page<CinemaDetailsDtoV1>> getCinemas(int pageNum, int pageSize) {
        String cacheKey = String.format("cinemas_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("cinemas");
        String LOG_PREFIX = "getCinemas";

        Page<CinemaDetailsDtoV1> cinemaDetailsDtos = cacheService.getCachedCinemaPage(cache, cacheKey, LOG_PREFIX);
        if (cinemaDetailsDtos != null) {
            return CompletableFuture.completedFuture(cinemaDetailsDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                cinemaDetailsDtos = cacheService.getCachedCinemaPage(cache, cacheKey, LOG_PREFIX);
                if (cinemaDetailsDtos != null) {
                    return CompletableFuture.completedFuture(cinemaDetailsDtos);
                }

                cinemaDetailsDtos = transactionTemplate.execute(status -> {
                    Page<Cinema> cinemas = cinemaRepository.findAll(PageRequest.of(pageNum, pageSize));
                    if (cinemas.isEmpty()) {
                        log.warn("{} :: No cinemas found. pageNum={}, pageSize={}.", LOG_PREFIX, pageNum, pageSize);
                        throw new CinemaNotFoundException("There's no cinema found.");
                    }

                    log.info("{} :: Found {} cinema(s). Caching data for key '{}'. pageNum={}, pageSize={}.",
                            LOG_PREFIX, cinemas.getTotalElements(), cacheKey, pageNum, pageSize);

                    return cinemas.map(CinemaMapper::fromCinemaToDetailsDtoV1);
                });

                cacheService.saveInCache(cache, cacheKey, cinemaDetailsDtos, LOG_PREFIX);

                return CompletableFuture.completedFuture(cinemaDetailsDtos);
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
    public CompletableFuture<CinemaDetailsDtoV1> getCinema(Long cinemaId) {
        String cacheKey = String.format("cinema_%d", cinemaId);
        Cache cache = cacheManager.getCache("cinema");
        String LOG_PREFIX = "getCinema";

        CinemaDetailsDtoV1 cinemaDetailsDtoV1 =
                cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, CinemaDetailsDtoV1.class);
        if (cinemaDetailsDtoV1 != null) {
            return CompletableFuture.completedFuture(cinemaDetailsDtoV1);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                cinemaDetailsDtoV1 =
                        cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, CinemaDetailsDtoV1.class);
                if (cinemaDetailsDtoV1 != null) {
                    return CompletableFuture.completedFuture(cinemaDetailsDtoV1);
                }

                cinemaDetailsDtoV1 = transactionTemplate.execute(status -> {
                    Cinema cinema = findCinemaById(cinemaId, LOG_PREFIX);
                    return CinemaMapper.fromCinemaToDetailsDtoV1(cinema);
                });

                cacheService.saveInCache(cache, cacheKey, cinemaDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(cinemaDetailsDtoV1);
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
    @CacheEvict(
            value = "cinemas",
            allEntries = true
    )
    public CinemaDetailsDtoV1 addCinema(CinemaManageDto cinemaManageDto) {
        String LOG_PREFIX = "addCinema";

        log.info("{} :: Evicting 'cinemas' cache and saving new cinema: {}", LOG_PREFIX, cinemaManageDto);

        Cinema cinema = CinemaMapper.fromCinemaManageDtoToCinema(cinemaManageDto);
        Cinema savedCinema = cinemaRepository.save(cinema);

        log.info("{} :: Successfully saved new cinema with ID={}.", LOG_PREFIX, savedCinema.getId());

        return CinemaMapper.fromCinemaToDetailsDtoV1(savedCinema);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "cinemas",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "cinema",
                            key = "'cinema_' + #cinemaId"
                    )
            }
    )
    public CinemaDetailsDtoV2 editCinema(CinemaManageDto cinemaManageDto, Long cinemaId) {
        String LOG_PREFIX = "editCinema";

        log.info("{} :: Evicting caches 'cinemas' (all entries) and 'cinema' with key 'cinema_{}'.",
                LOG_PREFIX, cinemaId);
        log.info("{} :: Editing cinema with ID={} and new data: {}", LOG_PREFIX, cinemaId, cinemaManageDto);

        Cinema cinema = findCinemaById(cinemaId, LOG_PREFIX);
        log.info("{} :: Found cinema with ID={}: {}", LOG_PREFIX, cinemaId, cinema);

        cinema.setName(cinemaManageDto.getName());
        cinema.setLocation(cinemaManageDto.getLocation());

        Cinema savedCinema = cinemaRepository.save(cinema);
        log.info("{} :: Successfully updated cinema with ID={}. New data: {}", LOG_PREFIX, cinemaId, cinema);

        return CinemaMapper.fromCinemaToDetailsDtoV2(savedCinema);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "cinemas",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "cinema",
                            key = "'cinema_' + #cinemaId"
                    )
            }
    )
    public void deleteCinema(Long cinemaId) {
        String LOG_PREFIX = "deleteCinema";

        log.info("{} :: Evicting caches 'cinemas' (all entries) and 'cinema' with key 'cinema_{}'.",
                LOG_PREFIX, cinemaId);
        log.info("{} :: Initiating deletion of cinema with ID={}.", LOG_PREFIX, cinemaId);

        Cinema cinema = findCinemaById(cinemaId, LOG_PREFIX);
        log.info("{} :: Cinema found with ID={}. Details: {}", LOG_PREFIX, cinemaId, cinema);

        cinemaRepository.delete(cinema);

        log.info("{} :: Successfully deleted cinema with ID={}.", LOG_PREFIX, cinemaId);
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Cinema findCinemaById(Long cinemaId, String LOG_PREFIX) {
        return cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.error("{} :: Cinema not found with id: {}", LOG_PREFIX, cinemaId);
                    return new CinemaNotFoundException("Cinema not found.");
                });
    }
}
