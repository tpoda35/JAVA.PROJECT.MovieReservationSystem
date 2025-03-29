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
public class Cinema {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String location;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    public Integer getRoomNum() {
        if (rooms == null) {
            return 0;
        } else {
            return rooms.size();
        }
    }
}
