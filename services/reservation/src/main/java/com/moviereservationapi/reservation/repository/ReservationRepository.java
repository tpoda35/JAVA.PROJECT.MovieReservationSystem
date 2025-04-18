package com.moviereservationapi.reservation.repository;

import com.moviereservationapi.reservation.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    void removeAllByShowtimeId(Long showtimeId);
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

}
