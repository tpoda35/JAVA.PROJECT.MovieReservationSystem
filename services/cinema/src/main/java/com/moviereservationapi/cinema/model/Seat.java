package com.moviereservationapi.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Room room;

}
