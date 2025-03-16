package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.mapper.MovieMapper;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.MovieRepository;
import com.moviereservationapi.movie.service.IMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final CacheManager cacheManager;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize) {
        String cacheKey = String.format("movies_page_%d_size_%d", pageNumber, pageSize);
        Cache cache = cacheManager.getCache("movies");

        ValueWrapper cachedResult;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            return CompletableFuture.completedFuture((Page<MovieDto>) cachedResult.get());
        }

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    return CompletableFuture.completedFuture((Page<MovieDto>) cachedResult.get());
                }

                Page<Movie> movies = movieRepository.findAll(PageRequest.of(pageNumber, pageSize));
                if (movies.isEmpty()) {
                    log.info("api/movies :: No movies found.");
                    throw new MovieNotFoundException("No movies found.");
                }

                log.info("api/movies :: {} movies found. Page {}, Size {}", movies.getTotalElements(), pageNumber, pageSize);
                Page<MovieDto> results = movies.map(MovieMapper::fromMovieToDto);
                if (cache != null) {
                    cache.put(cacheKey, results);
                }
                return CompletableFuture.completedFuture(results);
            } finally {
                locks.remove(cacheKey, lock);
            }
        }
    }

    @Override
    @Async
    public CompletableFuture<MovieDto> getMovie(Long movieId) {
        String cacheKey = String.format("movie_%d", movieId);
        Cache cache = cacheManager.getCache("movie");

        ValueWrapper cachedResult;
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            return CompletableFuture.completedFuture((MovieDto) cachedResult.get());
        }

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    return CompletableFuture.completedFuture((MovieDto) cachedResult.get());
                }

                Movie movie = movieRepository.findById(movieId)
                        .orElseThrow(() -> {
                            log.info("api/movies/movieId :: Movie not found with the id of {}.", movieId);
                            return new MovieNotFoundException("Movie not found.");
                        });

                log.info("api/movies/movieId :: Movie found with the id of {}", movieId);
                log.info("api/movies/movieId :: Movie data: title: {}, length: {}, release: {}.",
                        movie.getTitle(), movie.getLength(), movie.getRelease());

                MovieDto result = MovieMapper.fromMovieToDto(movie);
                if (cache != null) {
                    cache.put(cacheKey, result);
                }

                return CompletableFuture.completedFuture(result);
            }
            finally {
                locks.remove(cacheKey, lock);
            }
        }
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "movies",
                            allEntries = true
                    )
            }
    )
    public MovieDto addMovie(@Valid MovieManageDto movieManageDto) {
        Movie movie = MovieMapper.fromManageDtoToMovie(movieManageDto);
        Movie savedMovie = movieRepository.save(movie);

        log.info("api/movies/addMovie :: Saved Movie.");
        log.info("api/movies/addMovie :: Movie data: {}", movie);

        return MovieMapper.fromMovieToDto(savedMovie);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "movies",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "movie",
                            key = "'movie_' + #movieId"
                    )
            }
    )
    public MovieDto editMovie(Long movieId, @Valid MovieManageDto movieManageDto) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/editMovie/movieId :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });

        movie.setTitle(movieManageDto.getTitle());
        movie.setLength(movieManageDto.getLength());
        movie.setRelease(movieManageDto.getRelease());
        movie.setMovieGenre(movieManageDto.getMovieGenre());

        Movie savedMovie = movieRepository.save(movie);
        log.info("api/movies/editMovie/movieId :: Movie saved.");
        log.info("api/movies/editMovie/movieId :: Movie data: {}", movie);

        return MovieMapper.fromMovieToDto(savedMovie);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(
                            value = "movies",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "movie",
                            key = "'movie_' + #movieId"
                    )
            }
    )
    public void deleteMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/deleteMovie/movieId :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });

        movieRepository.delete(movie);
    }

    // Showtimes endpoint
}
