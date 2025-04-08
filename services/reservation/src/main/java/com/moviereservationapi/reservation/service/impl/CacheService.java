package com.moviereservationapi.reservation.service.impl;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import com.moviereservationapi.reservation.service.ICacheService;
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
    public Page<ReservationDetailsDtoV2> getCachedUserReservationsPage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for page key '{}'.", logPrefix, cacheKey);
        Cache.ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for page key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<ReservationDetailsDtoV2>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for page key '{}'.", logPrefix, cacheKey);
        return null;
    }
}
