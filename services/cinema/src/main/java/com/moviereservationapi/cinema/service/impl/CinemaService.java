package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.CinemaManageDto;
import com.moviereservationapi.cinema.dto.CinemaDetailsDto;
import com.moviereservationapi.cinema.dto.CinemaDto;
import com.moviereservationapi.cinema.exception.CinemaNotFoundException;
import com.moviereservationapi.cinema.mapper.CinemaMapper;
import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.repository.CinemaRepository;
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

    @Override
    @Async
    public CompletableFuture<Page<CinemaDetailsDto>> getCinemas(int pageNum, int pageSize) {
        String cacheKey = String.format("cinemas_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("cinemas");

        Page<CinemaDetailsDto> cinemaDetailsDtos = getCachedCinemaPage(cache, cacheKey, "api/cinemas");
        if (cinemaDetailsDtos != null) {
            return CompletableFuture.completedFuture(cinemaDetailsDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                cinemaDetailsDtos = getCachedCinemaPage(cache, cacheKey, "api/cinemas");
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

                if (cache != null) {
                    cache.put(cacheKey, cinemaDetailsDtos);
                    log.info("api/cinemas :: Cached cinema data for key: {}", cacheKey);
                }

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
    public CompletableFuture<CinemaDetailsDto> getCinema(Long cinemaId) {
        String cacheKey = String.format("cinema_%d", cinemaId);
        Cache cache = cacheManager.getCache("cinema");

        CinemaDetailsDto cinemaDetailsDto = getCachedCinema(cache, cacheKey, "api/cinemas/cinemaId");
        if (cinemaDetailsDto != null) {
            return CompletableFuture.completedFuture(cinemaDetailsDto);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            // The thread will wait for 10 sec max, 30 sec ttl/key.
            // Change lock to env variables.
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                cinemaDetailsDto = getCachedCinema(cache, cacheKey, "api/cinemas/cinemaId");
                if (cinemaDetailsDto != null) {
                    return CompletableFuture.completedFuture(cinemaDetailsDto);
                }

                cinemaDetailsDto = transactionTemplate.execute(status -> {
                    Cinema cinema = cinemaRepository.findById(cinemaId)
                            .orElseThrow(() -> {
                                log.error("api/cinemas/cinemaId :: Cinema not found with id: {}", cinemaId);
                                return new CinemaNotFoundException("Cinema not found.");
                            });

                    return CinemaMapper.fromCinemaToDetailsDto(cinema);
                });

                if (cache != null) {
                    cache.put(cacheKey, cinemaDetailsDto);
                    log.info("api/cinemas/cinemaId :: Cached cinema data for key: {}", cacheKey);
                }

                return CompletableFuture.completedFuture(cinemaDetailsDto);
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
    public CinemaDetailsDto addCinema(CinemaManageDto cinemaManageDto) {
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
    public CinemaDto editCinema(CinemaManageDto cinemaManageDto, Long cinemaId) {
        log.info("api/cinemas/cinemaId (editCinema) :: Evicting cache 'cinemas' and 'cinema' with the key of 'cinema_{}'", cinemaId);
        log.info("api/cinemas/cinemaId (editCinema) :: Editing cinema with the id of {} and data of {}", cinemaId, cinemaManageDto);

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.info("api/cinemas/cinemaId (editCinema) :: Movie not found with the id of {}.", cinemaId);
                    return new CinemaNotFoundException("Movie not found.");
                });
        log.info("api/cinemas/cinemaId (editCinema) :: Movie found with the id of {}.", cinemaId);

        cinema.setName(cinemaManageDto.getName());
        cinema.setLocation(cinemaManageDto.getLocation());

        Cinema savedCinema = cinemaRepository.save(cinema);
        log.info("api/cinemas/cinemaId (editCinema) :: Saved movie: {}", cinema);

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
        log.info("api/cinemas/cinemaId (deleteCinema) :: Evicting cache 'cinemas' and 'cinema' with the key of 'movie_{}'", cinemaId);
        log.info("api/cinemas/cinemaId (deleteCinema) :: Deleting cinema with the id of {}.", cinemaId);

        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> {
                    log.info("api/cinemas/cinemaId (deleteCinema) :: Cinema not found with the id of {}.", cinemaId);
                    return new CinemaNotFoundException("Cinema not found.");
                });
        log.info("api/cinemas/cinemaId (deleteCinema) :: Cinema found with the id of {} and data of {}.", cinemaId, cinema);

        cinemaRepository.delete(cinema);
    }

    private CinemaDetailsDto getCachedCinema(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (CinemaDetailsDto) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }

    private Page<CinemaDetailsDto> getCachedCinemaPage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<CinemaDetailsDto>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }
}
