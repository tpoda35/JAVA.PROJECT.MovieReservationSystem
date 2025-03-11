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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService implements IMovieService {

    private final MovieRepository movieRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Cacheable(
            value = "movies",
            key = "'movies_page_' + #pageNumber + '_size_' + #pageSize"
    )
    @Async
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> movies = movieRepository.findAll(pageable);

        if (movies.isEmpty()) {
            log.info("api/movies :: No movies found.");
            throw new MovieNotFoundException("No movies found.");
        }

        log.info("api/movies :: {} movies found.", movies.getTotalElements());
        log.info("api/movies :: Pagination settings: pageNumber: {}, pageSize: {}.", pageNumber, pageSize);

        List<MovieDto> movieDtos = transactionTemplate.execute(status ->
                movies.getContent().stream()
                        .map(
                                MovieMapper::fromMovieToDto
                        )
                        .collect(Collectors.toList())
        );

        if (movieDtos == null) {
            log.info("api/movies :: movieDto list is null.");
            throw new MovieNotFoundException("There's no movie found.");
        }

        return CompletableFuture.completedFuture(
                new PageImpl<>(movieDtos, pageable, movies.getTotalElements())
        );
    }

    @Override
    @Cacheable(
            value = "movie",
            key = "'movie_' + #movieId"
    )
    @Async
    public CompletableFuture<MovieDto> getMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.info("api/movies/movieId :: Movie not found with the id of {}.", movieId);
                    return new MovieNotFoundException("Movie not found.");
                });

        log.info("api/movies/movieId :: Movie found with the id of {}", movieId);
        log.info("api/movies/movieId :: Movie data: title: {}, length: {}, release: {}.",
                movie.getTitle(), movie.getLength(), movie.getRelease());

        return CompletableFuture.completedFuture(
                MovieMapper.fromMovieToDto(movie)
        );
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
}
