package com.moviereservationapi.movie.repository;

import com.moviereservationapi.movie.model.Movie;
import com.moviereservationapi.movie.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.userIds " +
            "WHERE m.id = :movieId")
    Optional<Movie> findByIdWithUserIdsAndReviews(@Param("movieId") Long movieId);

    @Query("SELECT r FROM Review r " +
            "WHERE r.movie.id = :movieId")
    Page<Review> findReviewsByMovieId(@Param("movieId") Long movieId, Pageable pageable);
}
