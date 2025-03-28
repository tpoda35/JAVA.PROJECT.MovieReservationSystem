package com.moviereservationapi.reservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
// Need more constraint.
// Problem: Users can reserve the same seat in the same showtime.
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "seatId"}))
public class ReservationSeat {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // Feign
    private Long seatId;

}
