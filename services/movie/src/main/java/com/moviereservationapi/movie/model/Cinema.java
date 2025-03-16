package com.moviereservationapi.movie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<Movie> movies = new ArrayList<>();
}
