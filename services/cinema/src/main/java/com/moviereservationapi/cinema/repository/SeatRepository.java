package com.moviereservationapi.cinema.repository;

import com.moviereservationapi.cinema.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT COUNT(e) FROM Seat e WHERE e.id IN :ids")
    long countByIdIn(List<Long> ids);

}
