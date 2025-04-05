package com.moviereservationapi.showtime.service.impl;

import com.moviereservationapi.showtime.dto.showtime.ShowtimeDetailsDtoV1;
import com.moviereservationapi.showtime.service.ICacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

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
    public Page<ShowtimeDetailsDtoV1> getCachedShowtimePage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<ShowtimeDetailsDtoV1>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }
}
