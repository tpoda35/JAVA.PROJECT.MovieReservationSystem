package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieDto;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;

public interface ICacheService {
    <T> void saveInCache(Cache cache, String cacheKey, T data, String logPrefix);
    <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type);
    Page<MovieDto> getCachedMoviePage(Cache cache, String cacheKey, String logPrefix);
}
