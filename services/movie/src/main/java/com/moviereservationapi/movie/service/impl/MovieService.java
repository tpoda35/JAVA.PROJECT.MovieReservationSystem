package com.moviereservationapi.movie.service.impl;

import com.moviereservationapi.movie.dto.MovieDto;
import com.moviereservationapi.movie.exception.MovieNotFoundException;
import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.repository.MovieRepository;
import com.moviereservationapi.movie.service.IMovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(
            cacheNames = "movies",
            key = "'movies_page_' + #pageNumber + '_size_' + #pageSize"
    )
    @Override
    @Async
    public CompletableFuture<Page<MovieDto>> getAllMovie(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Movie> movies = movieRepository.findAll(pageable);

        if (movies.isEmpty()) {
            log.info("api/movies :: No movies found.");
            throw new MovieNotFoundException("There's no movie found.");
        }

        List<MovieDto> movieDtos = transactionTemplate.execute(status ->
                movies.getContent().stream()
                        .map(
                                movie -> MovieDto.builder()
                                        .title(movie.getTitle())
                                        .length(movie.getLength())
                                        .release(movie.getRelease())
                                        .movieGenre(movie.getMovieGenre())
                                        .build()
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
}
