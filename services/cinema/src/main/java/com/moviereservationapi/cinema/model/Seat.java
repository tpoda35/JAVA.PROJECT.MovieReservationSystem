package com.moviereservationapi.cinema.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Version
    @ToString.Exclude
    private Long version;

}
