package com.moviereservationapi.reservation.service;

import com.moviereservationapi.reservation.dto.reservation.ReservationDetailsDtoV2;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;

public interface ICacheService {
    <T> void saveInCache(Cache cache, String cacheKey, T data, String logPrefix);
    <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type);
    Page<ReservationDetailsDtoV2> getCachedUserReservationsPage(Cache cache, String cacheKey, String logPrefix);
}
