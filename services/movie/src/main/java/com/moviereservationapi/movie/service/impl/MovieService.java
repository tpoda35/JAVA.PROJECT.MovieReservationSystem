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
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNum, int pageSize) {
        String cacheKey = String.format("movies_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("movies");

        ValueWrapper cachedResult;
        log.info("api/movies :: Checking cache (1) for key '{}'.", cacheKey);
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            log.info("api/movies :: Cache HIT for key '{}'. Returning cache.", cacheKey);
            return CompletableFuture.completedFuture((Page<MovieDto>) cachedResult.get());
        }
        log.info("api/movies :: Cache MISS for key '{}'.", cacheKey);

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                log.info("api/movies :: Checking cache (2) for key '{}'.", cacheKey);
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    log.info("api/movies :: Cache HIT for key '{}'. Returning cache.", cacheKey);
                    return CompletableFuture.completedFuture((Page<MovieDto>) cachedResult.get());
                }
                log.info("api/movies :: Cache MISS for key '{}'. Fetching from DB...", cacheKey);

                Page<Movie> movies = movieRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (movies.isEmpty()) {
                    log.info("api/movies :: No movies found.");
                    throw new MovieNotFoundException("There's no movie found.");
                }

                log.info("api/movies :: Found {} movies. Caching data for key '{}'.", movies.getTotalElements(), cacheKey);
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
        log.info("api/movies/movieId :: Checking cache (1) for key '{}'.", cacheKey);
        if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
            log.info("api/movies/movieId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
            return CompletableFuture.completedFuture((MovieDto) cachedResult.get());
        }
        log.info("api/movies/movieId :: Cache MISS for key '{}'.", cacheKey);

        Object lock = locks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                log.info("api/movies/movieId :: Checking cache (2) for key '{}'.", cacheKey);
                if (cache != null && (cachedResult = cache.get(cacheKey)) != null) {
                    log.info("api/movies/movieId :: Cache HIT for key '{}'. Returning cache.", cacheKey);
                    return CompletableFuture.completedFuture((MovieDto) cachedResult.get());
                }
                log.info("api/movies/movieId :: Cache MISS for key '{}'. Fetching from DB...", cacheKey);

                Movie movie = movieRepository.findById(movieId)
                        .orElseThrow(() -> {
                            log.info("api/movies/movieId :: Movie not found with the id of {}.", movieId);
                            return new MovieNotFoundException("Movie not found.");
                        });

                log.info("api/movies/movieId :: Movie found with the id of {}. Caching data for key '{}'", movieId, cacheKey);
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
    @CacheEvict(
            value = "movies",
            allEntries = true
    )
    public MovieDto addMovie(@Valid MovieManageDto movieManageDto) {
        log.info("api/movies/addMovie :: Evicting 'movies' cache. Saving new movie: {}", movieManageDto);

        Movie movie = MovieMapper.fromManageDtoToMovie(movieManageDto);
        Movie savedMovie = movieRepository.save(movie);

        log.info("api/movies/addMovie :: Saved Movie: {}.", movie);

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
        log.info("api/movies/editMovie/movieId :: Evicting cache 'movies' and 'movie' with the key of 'movie_{}'", movieId);
        log.info("api/movies/editMovie/movieId :: Editing movie with the id of {} and data of {}", movieId, movieManageDto);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/editMovie/movieId :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
        log.info("api/movies/editMovie/movieId :: Movie found with the id of {}.", movieId);

        movie.setTitle(movieManageDto.getTitle());
        movie.setLength(movieManageDto.getLength());
        movie.setRelease(movieManageDto.getRelease());
        movie.setMovieGenre(movieManageDto.getMovieGenre());

        Movie savedMovie = movieRepository.save(movie);
        log.info("api/movies/editMovie/movieId :: Saved movie: {}", movie);

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
        log.info("api/movies/deleteMovie/movieId :: Evicting cache 'movies' and 'movie' with the key of 'movie_{}'", movieId);
        log.info("api/movies/editMovie/movieId :: Deleting movie with the id of {}.", movieId);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/deleteMovie/movieId :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
        log.info("api/movies/editMovie/movieId :: Movie found with the id of {} and data of {}.", movieId, movie);

        movieRepository.delete(movie);
    }

    // Showtimes endpoint
}
