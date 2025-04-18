package com.moviereservationapi.movie.model;

import com.moviereservationapi.movie.Enum.MovieGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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

    @NotNull(message = "Movie duration cannot be null.")
    private Double duration;

    @NotNull(message = "Movie release cannot be null.")
    private LocalDateTime releaseDate;

    @Enumerated(EnumType.STRING)
    private MovieGenre movieGenre;

    @ElementCollection
    @CollectionTable(
            name = "movie_showtime",
            joinColumns = @JoinColumn(name = "movie_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "showtime_id"})
    )
    @Column(name = "showtime_id")
    @ToString.Exclude
    private List<Long> showtimeIds = new ArrayList<>();

    @Version
    @ToString.Exclude
    private Long version;

    public void addShowtimeId(Long showtimeId) {
        if (this.showtimeIds == null) {
            this.showtimeIds = new ArrayList<>();
        }
        if (!this.showtimeIds.contains(showtimeId)) {
            this.showtimeIds.add(showtimeId);
        }
    }
}
