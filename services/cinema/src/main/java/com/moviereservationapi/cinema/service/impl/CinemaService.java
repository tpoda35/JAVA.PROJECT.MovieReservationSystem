package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.CinemaDetailsDtoV2;
import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.exception.CinemaNotFoundException;
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
        String LOG_PREFIX = "api/cinemas";

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
                        log.info("api/cinemas :: No cinema found.");
                        throw new CinemaNotFoundException("There's no cinema found.");
                    }

                    log.info("api/cinemas :: Found {} cinemas. Caching data for key '{}'.",
                            cinemas.getTotalElements(), cacheKey);

                    return cinemas.map(CinemaMapper::fromCinemaToDetailsDto);
                });

                cacheService.saveInCache(cache, cacheKey, cinemaDetailsDtos, LOG_PREFIX);

                return CompletableFuture.completedFuture(cinemaDetailsDtos);
            } else {
                log.warn("api/cinemas :: Failed to acquire lock for key: {}", cacheKey);
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
    public CompletableFuture<CinemaDetailsDtoV1> getCinema(Long cinemaId) {
        String cacheKey = String.format("cinema_%d", cinemaId);
        Cache cache = cacheManager.getCache("cinema");
        String LOG_PREFIX = "api/cinemas/cinemaId";

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
                    Cinema cinema = cinemaRepository.findById(cinemaId)
                            .orElseThrow(() -> {
                                log.error("api/cinemas/cinemaId :: Cinema not found with id: {}", cinemaId);
                                return new CinemaNotFoundException("Cinema not found.");
                            });

                    return CinemaMapper.fromCinemaToDetailsDto(cinema);
                });

                cacheService.saveInCache(cache, cacheKey, cinemaDetailsDtoV1, LOG_PREFIX);

                return CompletableFuture.completedFuture(cinemaDetailsDtoV1);
            } else {
                log.warn("api/cinemas/cinemaId :: Failed to acquire lock for key: {}", cacheKey);
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
    @CacheEvict(
            value = "cinemas",
            allEntries = true
    )
    public CinemaDetailsDtoV1 addCinema(CinemaManageDto cinemaManageDto) {
        log.info("api/cinemas (addCinema) :: Evicting 'cinemas' cache. Saving new cinema: {}", cinemaManageDto);

        Cinema cinema = CinemaMapper.fromCinemaManageDtoToCinema(cinemaManageDto);
        Cinema savedCinema = cinemaRepository.save(cinema);

        log.info("api/cinemas (addCinema) :: Saved cinema: {}.", cinema);

        return CinemaMapper.fromCinemaToDetailsDto(savedCinema);
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
        log.info("api/cinemas/cinemaId (editCinema) :: Evicting cache 'cinemas' and 'cinema' with the key of 'cinema_{}'", cinemaId);
        log.info("api/cinemas/cinemaId (editCinema) :: Editing cinema with the id of {} and data of {}", cinemaId, cinemaManageDto);

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.info("api/cinemas/cinemaId (editCinema) :: Cinema not found with the id of {}.", cinemaId);
                    return new CinemaNotFoundException("Cinema not found.");
                });
        log.info("api/cinemas/cinemaId (editCinema) :: Cinema found with the id of {}.", cinemaId);

        cinema.setName(cinemaManageDto.getName());
        cinema.setLocation(cinemaManageDto.getLocation());

        Cinema savedCinema = cinemaRepository.save(cinema);
        log.info("api/cinemas/cinemaId (editCinema) :: Saved cinema: {}", cinema);

        return CinemaMapper.fromCinemaToCinemaDto(savedCinema);
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
        log.info("api/cinemas/cinemaId (deleteCinema) :: Evicting cache 'cinemas' and 'cinema' with the key of 'cinema_{}'", cinemaId);
        log.info("api/cinemas/cinemaId (deleteCinema) :: Deleting cinema with the id of {}.", cinemaId);

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.info("api/cinemas/cinemaId (deleteCinema) :: Cinema not found with the id of {}.", cinemaId);
                    return new CinemaNotFoundException("Cinema not found.");
                });
        log.info("api/cinemas/cinemaId (deleteCinema) :: Cinema found with the id of {} and data of {}.", cinemaId, cinema);

        cinemaRepository.delete(cinema);
    }
}
