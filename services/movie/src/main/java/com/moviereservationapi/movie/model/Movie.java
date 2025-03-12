package com.moviereservationapi.movie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moviereservationapi.movie.Enum.MovieGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
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

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Review> reviews;

    @ManyToMany
    @JoinTable(
            name = "movie_actor",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @ToString.Exclude
    private List<Actor> actors;

    // From another microservices.

    @ElementCollection
    @CollectionTable(
            name = "movie_users",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "user_id")
    @ToString.Exclude
    private List<Long> userIds;

    @ElementCollection
    @CollectionTable(
            name = "movie_showtimes",
            joinColumns = @JoinColumn(name = "movie_id")
    )
    @Column(name = "showtime_id")
    @ToString.Exclude
    private List<Long> showtimeIds;
}
