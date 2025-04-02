package com.moviereservationapi.cinema.service.impl;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import com.moviereservationapi.cinema.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CacheService implements ICacheService {

    @Override
    public <T> void saveInCache(Cache cache, String cacheKey, T data, String logPrefix) {
        if (cache != null) {
            cache.put(cacheKey, data);
            log.info("{} :: Cached data for key: {}", logPrefix, cacheKey);
        }
    }

    @Override
    public <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return type.cast(cachedResult.get());
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }

    @Override
    public Page<CinemaDetailsDtoV1> getCachedCinemaPage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for page key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for page key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<CinemaDetailsDtoV1>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for page key '{}'.", logPrefix, cacheKey);
        return null;
    }

    @Override
    public List<SeatDetailsDtoV1> getCachedSeatList(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for list key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for list key '{}'. Returning cache.", logPrefix, cacheKey);
            return (List<SeatDetailsDtoV1>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for list key '{}'.", logPrefix, cacheKey);
        return null;
    }

    @Override
    public Page<RoomDetailsDtoV1> getCachedRoomPage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for page key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for page key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<RoomDetailsDtoV1>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for page key '{}'.", logPrefix, cacheKey);
        return null;
    }
}
