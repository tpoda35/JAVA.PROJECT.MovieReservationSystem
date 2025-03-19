package com.moviereservationapi.cinema.repository;

import com.moviereservationapi.cinema.model.Room;
import com.moviereservationapi.cinema.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r.seat FROM Room r WHERE r.id = :roomId")
    List<Seat> findAllSeatsByRoomId(@Param("roomId") Long roomId);

}
