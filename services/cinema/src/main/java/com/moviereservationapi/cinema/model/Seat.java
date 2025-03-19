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
@Table
public class Seat {

    @Id
    @GeneratedValue
    private Long id;

    private String seatRow;

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
    private List<Long> reservationSeatids = new ArrayList<>();

}
