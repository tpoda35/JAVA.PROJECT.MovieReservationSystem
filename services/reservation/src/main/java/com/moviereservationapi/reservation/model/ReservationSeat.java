package com.moviereservationapi.reservation.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "seatId"}),
        indexes = {
                @Index(name = "idx_reservation_seat_seat_id", columnList = "seatId"),
                @Index(name = "idx_reservation_seat_reservation_id", columnList = "reservation_id")
        })
public class ReservationSeat {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    // Feign
    private Long seatId;

    @Version
    @ToString.Exclude
    private Long version;

}
