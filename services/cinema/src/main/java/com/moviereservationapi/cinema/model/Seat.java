package com.moviereservationapi.cinema.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
// ConstraintViolationException handle
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "seat_row", "seat_number"}))
public class Seat {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "seat_row", nullable = false)
    private String seatRow;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @ManyToOne
    @JoinColumn(
            name = "room_id",
            nullable = false)
    @ToString.Exclude
    private Room room;

    @ElementCollection
    @CollectionTable(
            name = "reservationSeat_seat",
            joinColumns = @JoinColumn(name = "seat_id")
    )
    @Column(name = "reservationSeat_id")
    @ToString.Exclude
    private List<Long> reservationSeatIds = new ArrayList<>();

    @Version
    @ToString.Exclude
    private Long version;

}
