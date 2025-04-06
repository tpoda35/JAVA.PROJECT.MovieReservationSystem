package com.moviereservationapi.reservation.repository;

import com.moviereservationapi.reservation.model.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    @Query("SELECT rs.seatId FROM ReservationSeat rs WHERE rs.reservation.showtimeId = :showtimeId")
    List<Long> findReservedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);

    Boolean existsBySeatIdInAndReservation_ShowtimeId(Collection<Long> seatIds, Long reservationShowtimeId);
}
