package com.moviereservationapi.movie.model;

import com.moviereservationapi.movie.Enum.MovieGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class Movie {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Movie title cannot be null.")
    @Length(min = 1, max = 100, message = "Movie name must be between 1 and 100 characters.")
    private String title;

    @NotNull(message = "Movie length cannot be null.")
    private Double length;

    @NotNull(message = "Movie release cannot be null.")
    private LocalDateTime release;

    @Enumerated(EnumType.STRING)
    private MovieGenre movieGenre;

    // Cinema, showtime connection
    @ManyToMany
    @JoinTable(
            name = "movie_cinema",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "cinema_id")
    )
    private List<Cinema> cinemas = new ArrayList<>();
}
