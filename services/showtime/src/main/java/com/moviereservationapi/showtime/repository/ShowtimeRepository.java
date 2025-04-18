package com.moviereservationapi.showtime.repository;

import com.moviereservationapi.showtime.model.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    Page<Showtime> findByMovieId(Long movieId, Pageable pageable);

    @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId AND " +
            "(:startTime < s.endTime AND :endTime > s.startTime)")
    List<Showtime> findOverlappingShowtimes(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

}
