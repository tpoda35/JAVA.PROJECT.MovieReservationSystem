package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.Enum.MovieGenre;
import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.exception.LockAcquisitionException;
import com.moviereservationapi.movie.exception.LockInterruptedException;
import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.mapper.MovieMapper;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.MovieRepository;
import com.moviereservationapi.movie.service.ICacheService;
import com.moviereservationapi.movie.service.IMovieService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;
    private final ICacheService cacheService;

    @Override
    @Async
    public CompletableFuture<Page<MovieDto>> getMovies(
            int pageNum,
            int pageSize,
            String title,
            LocalDateTime releaseAfter,
            Double durationGreaterThan,
            MovieGenre movieGenre
    ) {
        String LOG_PREFIX = "getMovies";

        String cacheKey = String.format(
                "movies_page_%d_size_%d_name_%s_after_%s_longer_%s_genre_%s",
                pageNum,
                pageSize,
                title != null ? title.toLowerCase().trim() : "any",
                releaseAfter != null ? releaseAfter.toString() : "any",
                durationGreaterThan != null ? durationGreaterThan : "any",
                movieGenre != null ? movieGenre.name() : "any"
        );
        Cache cache = cacheManager.getCache("movies");

        Page<MovieDto> movieDtos = cacheService.getCachedMoviePage(cache, cacheKey, LOG_PREFIX);
        if (movieDtos != null) {
            return CompletableFuture.completedFuture(movieDtos);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                movieDtos = cacheService.getCachedMoviePage(cache, cacheKey, LOG_PREFIX);
                if (movieDtos != null) {
                    return CompletableFuture.completedFuture(movieDtos);
                }

                Page<Movie> movies = movieRepository.findAll(PageRequest.of(pageNum, pageSize));
                if (movies.isEmpty()) {
                    log.info("{} :: No movies found.", LOG_PREFIX);
                    throw new MovieNotFoundException("There's no movie found.");
                }

                List<Movie> filteredList = movies.stream()
                        .filter(m -> title == null || m.getTitle().toLowerCase().contains(title.toLowerCase()))
                        .filter(m -> releaseAfter == null || m.getReleaseDate().isAfter(releaseAfter))
                        .filter(m -> durationGreaterThan == null || m.getDuration() > durationGreaterThan)
                        .filter(m -> movieGenre == null || m.getMovieGenre() == movieGenre)
                        .toList();

                Page<Movie> filteredPage = new PageImpl<>(
                        filteredList,
                        PageRequest.of(pageNum, pageSize),
                        filteredList.size()
                );

                log.info("{} :: Found {} movie(s). Caching data for key '{}'.", LOG_PREFIX, movies.getTotalElements(), cacheKey);
                movieDtos = filteredPage.map(MovieMapper::fromMovieToDto);

                cacheService.saveInCache(cache, cacheKey, movieDtos, LOG_PREFIX);

                return CompletableFuture.completedFuture(movieDtos);
            } else {
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Async
    public CompletableFuture<MovieDto> getMovie(Long movieId) {
        String LOG_PREFIX = "getMovie";

        String cacheKey = String.format("movie_%d", movieId);
        Cache cache = cacheManager.getCache("movie");

        MovieDto movieDto = cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, MovieDto.class);
        if (movieDto != null) {
            return CompletableFuture.completedFuture(movieDto);
        }

        RLock lock = redissonClient.getLock(cacheKey);
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                movieDto = cacheService.getCachedData(cache, cacheKey, LOG_PREFIX, MovieDto.class);
                if (movieDto != null) {
                    return CompletableFuture.completedFuture(movieDto);
                }

                Movie movie = findMovieById(movieId, LOG_PREFIX);
                log.info("{} :: Movie found with the id of {}. Caching data for key '{}'.", LOG_PREFIX, movieId, cacheKey);

                movieDto = MovieMapper.fromMovieToDto(movie);

                cacheService.saveInCache(cache, cacheKey, movieDto, LOG_PREFIX);

                return CompletableFuture.completedFuture(movieDto);
            } else {
                failedAcquireLock(LOG_PREFIX, cacheKey);
                throw new LockAcquisitionException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockInterruptedException("Thread interrupted while acquiring lock", e);
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
    @Transactional
    public MovieDto addMovie(@Valid MovieManageDto movieManageDto) {
        String LOG_PREFIX = "addMovie";

        log.info("{} :: Evicting 'movies' cache before saving new movie: {}", LOG_PREFIX, movieManageDto);

        Movie movie = MovieMapper.fromManageDtoToMovie(movieManageDto);
        Movie savedMovie = movieRepository.save(movie);

        log.info("{} :: Successfully saved movie with id {}: {}", LOG_PREFIX, savedMovie.getId(), savedMovie);

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
    @Transactional
    public MovieDto editMovie(Long movieId, @Valid MovieManageDto movieManageDto) {
        String LOG_PREFIX = "editMovie";

        log.info("{} :: Evicting 'movies' and 'movie' cache for movieId {}.", LOG_PREFIX, movieId);
        log.info("{} :: Editing movie with id {} using data: {}", LOG_PREFIX, movieId, movieManageDto);

        Movie movie = findMovieById(movieId, LOG_PREFIX);
        log.info("{} :: Found movie with id {}: {}", LOG_PREFIX, movieId, movie);

        movie.setTitle(movieManageDto.getTitle());
        movie.setDuration(movieManageDto.getLength());
        movie.setReleaseDate(movieManageDto.getRelease());
        movie.setMovieGenre(movieManageDto.getMovieGenre());

        Movie savedMovie = movieRepository.save(movie);
        log.info("{} :: Successfully saved updated movie with id {}: {}", LOG_PREFIX, savedMovie.getId(), savedMovie);

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
    @Transactional
    public void deleteMovie(Long movieId) {
        String LOG_PREFIX = "deleteMovie";

        log.info("{} :: Evicting 'movies' and 'movie' cache for movieId {}.", LOG_PREFIX, movieId);
        log.info("{} :: Attempting to delete movie with id {}.", LOG_PREFIX, movieId);

        Movie movie = findMovieById(movieId, LOG_PREFIX);
        log.info("{} :: Movie found with id {}: {}", LOG_PREFIX, movieId, movie);

        movieRepository.delete(movie);

        log.info("{} :: Movie with id {} deleted successfully.", LOG_PREFIX, movieId);
    }

    private void failedAcquireLock(String LOG_PREFIX, String cacheKey) {
        log.warn("{} :: Failed to acquire lock for key: {}", LOG_PREFIX, cacheKey);
    }

    private Movie findMovieById(Long movieId, String LOG_PREFIX) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("{} :: Movie not found with the id of {}.", LOG_PREFIX, movieId);
                    return new MovieNotFoundException("Movie not found.");
                });
    }
}
