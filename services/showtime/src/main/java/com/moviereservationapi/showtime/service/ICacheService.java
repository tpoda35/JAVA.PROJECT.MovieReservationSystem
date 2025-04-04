package com.moviereservationapi.showtime.service;

import com.moviereservationapi.showtime.dto.ShowtimeDetailsDtoV1;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;

public interface ICacheService {
    <T> void saveInCache(Cache cache, String cacheKey, T data, String logPrefix);
    <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type);
    Page<ShowtimeDetailsDtoV1> getCachedShowtimePage(Cache cache, String cacheKey, String logPrefix);
}
