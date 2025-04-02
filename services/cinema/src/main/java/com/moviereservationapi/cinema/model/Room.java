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
public class Room {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Integer totalSeat;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    @ToString.Exclude
    private Cinema cinema;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Seat> seat = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "room_showtime",
            joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "showtime_id")
    @ToString.Exclude
    private List<Long> showtimeIds = new ArrayList<>();
}
