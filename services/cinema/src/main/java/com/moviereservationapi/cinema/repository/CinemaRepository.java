package com.moviereservationapi.cinema.repository;

import com.moviereservationapi.cinema.model.Cinema;
import com.moviereservationapi.cinema.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    @Query("SELECT r FROM Room r WHERE r.cinema.id = :cinemaId")
    Page<Room> findAllRoomsByCinemaId(@Param("cinemaId") Long cinemaId, Pageable pageable);

}
