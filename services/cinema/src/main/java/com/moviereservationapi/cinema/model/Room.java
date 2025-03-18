package com.moviereservationapi.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
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
    private Cinema cinema;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<Seat> seat;

    @ElementCollection
    @CollectionTable(
            name = "room_showtime",
            joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "showtime_id")
    private List<Long> showtimeIds;
}
