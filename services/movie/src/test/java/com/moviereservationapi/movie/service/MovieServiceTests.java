package com.moviereservationapi.movie.service;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.exception.LockAcquisitionException;
import com.moviereservationapi.movie.exception.LockInterruptedException;
import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.repository.MovieRepository;
import com.moviereservationapi.movie.service.impl.CacheService;
import com.moviereservationapi.movie.service.impl.MovieService;
import com.moviereservationapi.movie.utility.MovieUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTests {

    @Mock private MovieRepository movieRepository;

    @Mock private CacheManager cacheManager;

    @Mock private CacheService cacheService;

    @Mock private Cache cache;

    @Mock private RedissonClient redissonClient;

    @Mock private RLock rLock;

    @InjectMocks private MovieService movieService;

    @Test
    void shouldReturnMoviesFromCacheWhenPresent() throws ExecutionException, InterruptedException {
        // Arrange
        Page<MovieDto> cachedPage = new PageImpl<>(List.of(MovieUtility.getMovieDto()));

        when(cacheManager.getCache("movies")).thenReturn(cache);
        when(cacheService.getCachedMoviePage(any(), anyString(), anyString())).thenReturn(cachedPage);

        // Act
        CompletableFuture<Page<MovieDto>> resultFuture = movieService.getMovies(0, 10, null, null, null, null);
        Page<MovieDto> result = resultFuture.get();

        // Assert
        assertEquals(1, result.getTotalElements());
        verifyNoInteractions(movieRepository);
    }

    @Test
    void shouldAttemptLockWhenCacheMissOccurs() throws InterruptedException {
        // Arrange
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cacheService.getCachedMoviePage(eq(cache), anyString(), anyString())).thenReturn(null);

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);

        // Act & Assert
        assertThrows(LockAcquisitionException.class, () -> movieService.getMovies(0, 10, null, null, null, null));

        verify(redissonClient).getLock(anyString());
        verify(rLock).tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldReturnMoviesFromCacheAfterLockWhenPresent() throws InterruptedException {
        // Arrange
        Page<MovieDto> cachedPage = new PageImpl<>(List.of(MovieUtility.getMovieDto()));

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cacheService.getCachedMoviePage(eq(cache), anyString(), anyString()))
                .thenReturn(null)
                .thenReturn(cachedPage);

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // Act
        CompletableFuture<Page<MovieDto>> resultFuture = movieService.getMovies(
                0, 10, null, null, null, null
        );

        // Assert
        Page<MovieDto> result = resultFuture.join();

        assertEquals(result, cachedPage);
        verify(cacheService, times(2))
                .getCachedMoviePage(eq(cache), anyString(), anyString());
        verifyNoInteractions(movieRepository);
        verify(rLock).unlock();
    }

    @Test
    void shouldThrowExceptionWhenLockNotAcquired() throws InterruptedException {
        // Arrange
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cacheService.getCachedMoviePage(eq(cache), anyString(), anyString())).thenReturn(null);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock( anyLong(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(false);

        // Act & Assert
        LockAcquisitionException exception = assertThrows(LockAcquisitionException.class, () -> movieService.getMovies(0, 10, null, null, null, null));

        assertEquals("Failed to acquire lock", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenThreadInterruptedDuringLock() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenThrow(new InterruptedException());

        assertThrows(LockInterruptedException.class, () -> movieService.getMovies(0, 10, null, null, null, null).join());
        assertTrue(Thread.currentThread().isInterrupted());

    }

    @Test
    void shouldThrowMovieNotFoundExceptionWhenMoviesNotFound() throws InterruptedException {
        // Arrange
        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cacheService.getCachedMoviePage(eq(cache), anyString(), anyString())).thenReturn(null);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        when(movieRepository.findAll(PageRequest.of(0, 10))).thenReturn(Page.empty());
        // Act & Assert
        assertThrows(MovieNotFoundException.class, () -> movieService.getMovies(0, 10, null, null, null, null).join());
    }
}
