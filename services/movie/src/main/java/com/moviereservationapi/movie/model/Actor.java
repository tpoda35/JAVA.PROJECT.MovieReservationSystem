package com.moviereservationapi.movie.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class Actor {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "First name cannot be null.")
    private String firstName;

    @NotNull(message = "Last name cannot be null.")
    private String lastName;

    private String fullName;

    @ManyToMany(mappedBy = "actors")
    private List<Movie> movies;

    @PrePersist
    protected void onCreate() {
        fullName = firstName + " " + lastName;
    }
}
