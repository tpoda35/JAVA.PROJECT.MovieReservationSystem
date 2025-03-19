package com.moviereservationapi.cinema.repository;

import com.moviereservationapi.cinema.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findSeatsByRoomId(Long roomId);

}
