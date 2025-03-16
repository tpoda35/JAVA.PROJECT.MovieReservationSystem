package com.moviereservationapi.movie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Cinema {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Name field cannot be empty.")
    private String name;

    @NotBlank(message = "Address field cannot be empty.")
    private String address;

    @ManyToMany(mappedBy = "cinemas")
    @ToString.Exclude
    private List<Movie> movies = new ArrayList<>();

    @Version
    @ToString.Exclude
    private Integer version;
}
