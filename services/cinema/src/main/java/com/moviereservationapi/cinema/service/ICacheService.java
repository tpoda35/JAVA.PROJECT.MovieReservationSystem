package com.moviereservationapi.cinema.service;

import com.moviereservationapi.cinema.dto.cinema.CinemaDetailsDtoV1;
import com.moviereservationapi.cinema.dto.room.RoomDetailsDtoV1;
import com.moviereservationapi.cinema.dto.seat.SeatDetailsDtoV1;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ICacheService {
    <T> void saveInCache(Cache cache, String cacheKey, T data, String logPrefix);
    <T> T getCachedData(Cache cache, String cacheKey, String logPrefix, Class<T> type);
    Page<CinemaDetailsDtoV1> getCachedCinemaPage(Cache cache, String cacheKey, String logPrefix);
    List<SeatDetailsDtoV1> getCachedSeatList(Cache cache, String cacheKey, String logPrefix);
    Page<RoomDetailsDtoV1> getCachedRoomPage(Cache cache, String cacheKey, String logPrefix);
}
