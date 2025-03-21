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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    @Override
    @Async
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNum, int pageSize) {
        String cacheKey = String.format("movies_page_%d_size_%d", pageNum, pageSize);
        Cache cache = cacheManager.getCache("movies");

        Page<MovieDto> movieDtos = getCachedMoviePage(cache, cacheKey, "api/movies");
        if (movieDtos != null) {
            return CompletableFuture.completedFuture(movieDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                movieDtos = getCachedMoviePage(cache, cacheKey, "api/movies");
                if (movieDtos != null) {
                    return CompletableFuture.completedFuture(movieDtos);
                }

                Page<Movie> movies = movieRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (movies.isEmpty()) {
                    log.info("api/movies :: No movies found.");
                    throw new MovieNotFoundException("There's no movie found.");
                }

                log.info("api/movies :: Found {} movies. Caching data for key '{}'.", movies.getTotalElements(), cacheKey);
                movieDtos = movies.map(MovieMapper::fromMovieToDto);
                if (cache != null) {
                    cache.put(cacheKey, movieDtos);
                }

                return CompletableFuture.completedFuture(movieDtos);
            } else {
                log.warn("api/movies :: Failed to acquire lock for key: {}", cacheKey);
                throw new RuntimeException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Async
    public CompletableFuture<MovieDto> getMovie(Long movieId) {
        String cacheKey = String.format("movie_%d", movieId);
        Cache cache = cacheManager.getCache("movie");

        MovieDto movieDto = getCachedMovie(cache, cacheKey, "api/movies/movieId");
        if (movieDto != null) {
            return CompletableFuture.completedFuture(movieDto);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                movieDto = getCachedMovie(cache, cacheKey, "api/movies/movieId");
                if (movieDto != null) {
                    return CompletableFuture.completedFuture(movieDto);
                }

                Movie movie = movieRepository.findById(movieId)
                        .orElseThrow(() -> {
                            log.info("api/movies/movieId :: Movie not found with the id of {}.", movieId);
                            return new MovieNotFoundException("Movie not found.");
                        });

                log.info("api/movies/movieId :: Movie found with the id of {}. Caching data for key '{}'", movieId, cacheKey);
                movieDto = MovieMapper.fromMovieToDto(movie);
                if (cache != null) {
                    cache.put(cacheKey, movieDto);
                }

                return CompletableFuture.completedFuture(movieDto);
            } else {
                log.warn("api/movies/movieId :: Failed to acquire lock for key: {}", cacheKey);
                throw new RuntimeException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @CacheEvict(
            value = "movies",
            allEntries = true
    )
    public MovieDto addMovie(@Valid MovieManageDto movieManageDto) {
        log.info("api/movies (addMovie) :: Evicting 'movies' cache. Saving new movie: {}", movieManageDto);

        Movie movie = MovieMapper.fromManageDtoToMovie(movieManageDto);
        Movie savedMovie = movieRepository.save(movie);

        log.info("api/movies (addMovie) :: Saved Movie: {}.", movie);

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
        log.info("api/movies/movieId (editMovie) :: Evicting cache 'movies' and 'movie' with the key of 'movie_{}'", movieId);
        log.info("api/movies/movieId (editMovie) :: Editing movie with the id of {} and data of {}", movieId, movieManageDto);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/movieId (editMovie) :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
        log.info("api/movies/movieId (editMovie) :: Movie found with the id of {}.", movieId);

        movie.setTitle(movieManageDto.getTitle());
        movie.setDuration(movieManageDto.getLength());
        movie.setReleaseDate(movieManageDto.getRelease());
        movie.setMovieGenre(movieManageDto.getMovieGenre());

        Movie savedMovie = movieRepository.save(movie);
        log.info("api/movies/movieId (editMovie) :: Saved movie: {}", movie);

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
        log.info("api/movies/movieId (deleteMovie) :: Evicting cache 'movies' and 'movie' with the key of 'movie_{}'", movieId);
        log.info("api/movies/movieId (deleteMovie) :: Deleting movie with the id of {}.", movieId);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/movieId (deleteMovie) :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
        log.info("api/movies/movieId (deleteMovie) :: Movie found with the id of {} and data of {}.", movieId, movie);

        movieRepository.delete(movie);
    }

    private Page<MovieDto> getCachedMoviePage(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for key '{}'.", logPrefix, cacheKey);
        ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for key '{}'. Returning cache.", logPrefix, cacheKey);
            return (Page<MovieDto>) cachedResult.get();
        }

        log.info("{} :: Cache MISS for key '{}'.", logPrefix, cacheKey);
        return null;
    }

    private MovieDto getCachedMovie(Cache cache, String cacheKey, String logPrefix) {
        if (cache == null) {
            return null;
        }

        log.info("{} :: Checking cache for list key '{}'.", logPrefix, cacheKey);
        ValueWrapper cachedResult = cache.get(cacheKey);

        if (cachedResult != null) {
            log.info("{} :: Cache HIT for list key '{}'. Returning cache.", logPrefix, cacheKey);
            return (MovieDto) cachedResult.get();
        }

        log.info("{} :: Cache MISS for list key '{}'.", logPrefix, cacheKey);
        return null;
    }

}
