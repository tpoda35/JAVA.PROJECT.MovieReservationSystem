package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.dto.MovieManageDto;
import com.moviereservationapi.movie.dto.ReviewDto;
import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.exception.ReviewNotFoundException;
import com.moviereservationapi.movie.feign.UserClient;
import com.moviereservationapi.movie.feignResponse.AppUserDto;
import com.moviereservationapi.movie.mapper.MovieMapper;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.model.Review;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final CacheManager cacheManager;
    private final TransactionTemplate transactionTemplate;
    private final UserClient userClient;

    private final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    @Override
    @Async
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize) {
        String cacheKey = String.format("movies_page_%d_size_%d", pageNumber, pageSize);
        Cache cache = cacheManager.getCache("movies");

        ValueWrapper cachedResult = null;
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

        ValueWrapper cachedResult = null;
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

    @Override
    @Async
    public CompletableFuture<Page<ReviewDto>> getMovieReviews(
            Long movieId, int pageNum, int pageSize
    ) {
        String movieReviewCacheKey = String.format("movie_reviews_page_%d_size_%d_id_%d", pageNum, pageSize, movieId);
        Cache movieReviewsCache = cacheManager.getCache("movie_reviews");
        Cache userCache = cacheManager.getCache("users");

        ValueWrapper wrapper = null;
        if (movieReviewsCache != null && (wrapper = movieReviewsCache.get(movieReviewCacheKey)) != null) {
            return CompletableFuture.completedFuture(
                    (Page<ReviewDto>) wrapper.get()
            );
        }

        Object lock = locks.computeIfAbsent(movieReviewCacheKey, k -> new Object());
        synchronized (lock) {
            if (movieReviewsCache != null && (wrapper = movieReviewsCache.get(movieReviewCacheKey)) != null) {
                return CompletableFuture.completedFuture(
                        (Page<ReviewDto>) wrapper.get()
                );
            }

            List<AppUserDto> appUserDtos = null;
            Movie movie = movieRepository.findByIdWithUserIdsAndReviews(movieId)
                            .orElseThrow(() -> new MovieNotFoundException("Movie not found."));
            Page<Review> reviews = movieRepository.findReviewsByMovieId(movieId, PageRequest.of(pageNum, pageSize));

            if (reviews.isEmpty()) {
                throw new ReviewNotFoundException("There are no reviews found for this movie.");
            }

            if (userCache != null) {
                String userCacheKey = String.format("users_%s", movie.getUserIds().stream()
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining("_")));
                if ((wrapper = userCache.get(userCacheKey)) != null) {
                    appUserDtos = (List<AppUserDto>) wrapper.get();
                    return CompletableFuture.completedFuture(createReviewDtos(
                            appUserDtos, pageNum, pageSize, movieId, reviews
                    ));
                }
            }

            return userClient.getUsers(movie.getUserIds())
                    .thenApply(dtos -> {
                        Page<ReviewDto> reviewDtos = createReviewDtos(dtos, pageNum, pageSize, movieId, reviews);
                        if (movieReviewsCache != null) {
                            movieReviewsCache.put(movieReviewCacheKey, reviewDtos);
                        }
                        return reviewDtos;
                    });
        }
    }

    private Page<ReviewDto> createReviewDtos(
            List<AppUserDto> appUserDtos, int pageNum, int pageSize, Long movieId, Page<Review> reviews
    ){
        List<ReviewDto> reviewDtos = reviews.getContent().stream()
                .map(review -> {
                    String email = findEmailByUserId(appUserDtos, review.getUserId());
                    return new ReviewDto(
                            email,
                            review.getContent(),
                            review.getCreatedAt()
                    );
                })
                .toList();

        return new PageImpl<>(reviewDtos, PageRequest.of(pageNum, pageSize), reviews.getTotalElements());
    }

    private String findEmailByUserId(List<AppUserDto> appUserDtos, Long userId) {
        return appUserDtos.stream()
                .filter(user -> user.getId().equals(userId))
                .map(AppUserDto::getEmail)
                .findFirst()
                .orElse(null);
    }
}
